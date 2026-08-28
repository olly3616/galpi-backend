package com.galpi.galpibackend.domain.schedule.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.galpi.galpibackend.domain.quote.entity.Quote;
import com.galpi.galpibackend.domain.quote.entity.Visibility;
import com.galpi.galpibackend.domain.schedule.entity.QuoteSchedule;
import com.galpi.galpibackend.domain.schedule.entity.RepeatType;
import com.galpi.galpibackend.domain.schedule.notification.NotificationSender;
import com.galpi.galpibackend.domain.schedule.repository.QuoteScheduleRepository;
import com.galpi.galpibackend.domain.work.entity.BookSource;
import com.galpi.galpibackend.domain.work.entity.BookType;
import com.galpi.galpibackend.domain.work.entity.Work;
import java.time.DayOfWeek;
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
    private NotificationSender notificationSender;

    @InjectMocks
    private NotificationDispatchService dispatchService;

    private QuoteSchedule schedule(RepeatType type, String daysOfWeek, LocalDateTime lastSentAt) {
        Work work = Work.builder().source(BookSource.API).type(BookType.NOVEL)
                .title("데미안").author("헤르만 헤세").build();
        ReflectionTestUtils.setField(work, "id", 10L);
        Quote quote = Quote.builder().userId(1L).work(work)
                .content("새는 알에서...").visibility(Visibility.PRIVATE).build();
        ReflectionTestUtils.setField(quote, "id", 100L);
        QuoteSchedule schedule = QuoteSchedule.builder()
                .userId(1L).quote(quote).sendTime(LocalTime.of(8, 0))
                .repeatType(type).daysOfWeek(daysOfWeek).isActive(true).build();
        if (lastSentAt != null) {
            schedule.markSent(lastSentAt);
        }
        return schedule;
    }

    @Test
    @DisplayName("DAILY 알림은 요일과 무관하게 발송 대상이다")
    void isDue_daily() {
        assertThat(dispatchService.isDue(schedule(RepeatType.DAILY, null, null), DayOfWeek.MONDAY)).isTrue();
    }

    @Test
    @DisplayName("WEEKLY 알림은 오늘 요일이 목록에 있을 때만 발송 대상이다")
    void isDue_weekly() {
        QuoteSchedule s = schedule(RepeatType.WEEKLY, "MON,WED,FRI", null);
        assertThat(dispatchService.isDue(s, DayOfWeek.WEDNESDAY)).isTrue();
        assertThat(dispatchService.isDue(s, DayOfWeek.TUESDAY)).isFalse();
    }

    @Test
    @DisplayName("ONCE 알림은 아직 발송된 적 없을 때만 발송 대상이다")
    void isDue_once() {
        assertThat(dispatchService.isDue(schedule(RepeatType.ONCE, null, null), DayOfWeek.MONDAY)).isTrue();
        assertThat(dispatchService.isDue(
                schedule(RepeatType.ONCE, null, LocalDateTime.of(2026, 8, 27, 8, 0)), DayOfWeek.MONDAY)).isFalse();
    }

    @Test
    @DisplayName("발송 시각이 되면 알림을 보내고 last_sent_at을 갱신한다")
    void dispatchDue_sendsAndMarks() {
        LocalDateTime now = LocalDateTime.of(2026, 8, 28, 8, 0); // 금요일
        QuoteSchedule due = schedule(RepeatType.DAILY, null, null);
        given(scheduleRepository.findByIsActiveTrueAndSendTime(LocalTime.of(8, 0)))
                .willReturn(List.of(due));

        int sent = dispatchService.dispatchDue(now);

        assertThat(sent).isEqualTo(1);
        verify(notificationSender).send(eq(1L), eq("데미안"), eq("새는 알에서..."), eq(100L));
        assertThat(due.getLastSentAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("오늘 이미 발송된 알림은 다시 보내지 않는다")
    void dispatchDue_skipsAlreadySentToday() {
        LocalDateTime now = LocalDateTime.of(2026, 8, 28, 8, 0);
        QuoteSchedule alreadySent = schedule(RepeatType.DAILY, null, LocalDateTime.of(2026, 8, 28, 8, 0));
        given(scheduleRepository.findByIsActiveTrueAndSendTime(LocalTime.of(8, 0)))
                .willReturn(List.of(alreadySent));

        int sent = dispatchService.dispatchDue(now);

        assertThat(sent).isZero();
        verify(notificationSender, never()).send(any(), any(), any(), any());
    }
}
