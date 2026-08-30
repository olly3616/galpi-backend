package com.galpi.galpibackend.domain.schedule.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.galpi.galpibackend.domain.quote.entity.Quote;
import com.galpi.galpibackend.domain.quote.entity.Visibility;
import com.galpi.galpibackend.domain.schedule.entity.QuoteSchedule;
import com.galpi.galpibackend.domain.schedule.entity.RepeatType;
import com.galpi.galpibackend.domain.schedule.repository.QuoteScheduleRepository;
import com.galpi.galpibackend.domain.work.entity.BookSource;
import com.galpi.galpibackend.domain.work.entity.Work;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class NotificationDispatchServiceTest {

    @Mock
    private QuoteScheduleRepository scheduleRepository;

    @Mock
    private ScheduleDispatchExecutor dispatchExecutor;

    @InjectMocks
    private NotificationDispatchService dispatchService;

    private QuoteSchedule schedule(long id, RepeatType type, String daysOfWeek, LocalDateTime lastSentAt) {
        Work work = Work.builder().source(BookSource.API)
                .title("데미안").author("헤르만 헤세").build();
        ReflectionTestUtils.setField(work, "id", 10L);
        Quote quote = Quote.builder().userId(1L).work(work)
                .content("새는 알에서...").visibility(Visibility.PRIVATE).build();
        ReflectionTestUtils.setField(quote, "id", 100L);
        QuoteSchedule schedule = QuoteSchedule.builder()
                .userId(1L).quote(quote).sendTime(LocalTime.of(8, 0))
                .repeatType(type).daysOfWeek(daysOfWeek).isActive(true).build();
        ReflectionTestUtils.setField(schedule, "id", id);
        if (lastSentAt != null) {
            schedule.markSent(lastSentAt);
        }
        return schedule;
    }

    // 2026-08-28은 금요일 (아래 요일 계산 기준)
    private static final LocalDate FRIDAY = LocalDate.of(2026, 8, 28);
    private static final LocalDate WEDNESDAY = LocalDate.of(2026, 8, 26);
    private static final LocalDate TUESDAY = LocalDate.of(2026, 8, 25);

    @Test
    @DisplayName("DAILY 알림은 요일과 무관하게 발송 대상이다")
    void isDue_daily() {
        assertThat(dispatchService.isDue(schedule(1L, RepeatType.DAILY, null, null), FRIDAY)).isTrue();
    }

    @Test
    @DisplayName("WEEKLY 알림은 오늘 요일이 목록에 있을 때만 발송 대상이다")
    void isDue_weekly() {
        QuoteSchedule s = schedule(1L, RepeatType.WEEKLY, "MON,WED,FRI", null);
        assertThat(dispatchService.isDue(s, WEDNESDAY)).isTrue();
        assertThat(dispatchService.isDue(s, TUESDAY)).isFalse();
    }

    @Test
    @DisplayName("ONCE 알림은 지정한 날짜에, 아직 발송된 적 없을 때만 발송 대상이다")
    void isDue_once() {
        QuoteSchedule once = schedule(1L, RepeatType.ONCE, null, null);
        ReflectionTestUtils.setField(once, "sendDate", FRIDAY);

        // 지정 날짜 + 미발송 → 발송 대상
        assertThat(dispatchService.isDue(once, FRIDAY)).isTrue();
        // 다른 날짜면 발송 대상 아님
        assertThat(dispatchService.isDue(once, WEDNESDAY)).isFalse();

        // 지정 날짜여도 이미 발송됐으면 대상 아님
        QuoteSchedule sent = schedule(1L, RepeatType.ONCE, null, LocalDateTime.of(2026, 8, 28, 8, 0));
        ReflectionTestUtils.setField(sent, "sendDate", FRIDAY);
        assertThat(dispatchService.isDue(sent, FRIDAY)).isFalse();

        // sendDate가 없으면(구 데이터) 발송하지 않는다
        QuoteSchedule noDate = schedule(1L, RepeatType.ONCE, null, null);
        assertThat(dispatchService.isDue(noDate, FRIDAY)).isFalse();
    }

    @Test
    @DisplayName("발송 시각이 되면 대상 알림을 건별 실행기로 위임한다")
    void dispatchDue_delegatesToExecutor() {
        LocalDateTime now = LocalDateTime.of(2026, 8, 28, 8, 0); // 금요일
        QuoteSchedule due = schedule(1L, RepeatType.DAILY, null, null);
        given(scheduleRepository.findActiveBySendTimeWithQuote(LocalTime.of(8, 0)))
                .willReturn(List.of(due));

        int dispatched = dispatchService.dispatchDue(now);

        assertThat(dispatched).isEqualTo(1);
        verify(dispatchExecutor).dispatchOne(1L, now);
    }

    @Test
    @DisplayName("오늘 이미 발송된 알림은 실행기로 위임하지 않는다")
    void dispatchDue_skipsAlreadySentToday() {
        LocalDateTime now = LocalDateTime.of(2026, 8, 28, 8, 0);
        QuoteSchedule alreadySent = schedule(1L, RepeatType.DAILY, null, LocalDateTime.of(2026, 8, 28, 8, 0));
        given(scheduleRepository.findActiveBySendTimeWithQuote(LocalTime.of(8, 0)))
                .willReturn(List.of(alreadySent));

        int dispatched = dispatchService.dispatchDue(now);

        assertThat(dispatched).isZero();
        verify(dispatchExecutor, never()).dispatchOne(any(), any());
    }

    @Test
    @DisplayName("한 건 발송이 실패해도 나머지 발송은 계속된다")
    void dispatchDue_continuesOnFailure() {
        LocalDateTime now = LocalDateTime.of(2026, 8, 28, 8, 0);
        QuoteSchedule a = schedule(1L, RepeatType.DAILY, null, null);
        QuoteSchedule b = schedule(2L, RepeatType.DAILY, null, null);
        given(scheduleRepository.findActiveBySendTimeWithQuote(LocalTime.of(8, 0)))
                .willReturn(List.of(a, b));
        willThrow(new RuntimeException("FCM 오류")).given(dispatchExecutor).dispatchOne(eq(1L), any());

        int dispatched = dispatchService.dispatchDue(now);

        assertThat(dispatched).isEqualTo(1); // b는 정상 발송
        verify(dispatchExecutor).dispatchOne(2L, now);
    }
}
