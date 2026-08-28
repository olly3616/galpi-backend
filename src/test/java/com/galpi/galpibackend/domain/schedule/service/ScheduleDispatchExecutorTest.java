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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ScheduleDispatchExecutorTest {

    @Mock
    private QuoteScheduleRepository scheduleRepository;

    @Mock
    private NotificationSender notificationSender;

    @InjectMocks
    private ScheduleDispatchExecutor executor;

    private QuoteSchedule schedule(LocalDateTime lastSentAt) {
        Work work = Work.builder().source(BookSource.API).type(BookType.NOVEL)
                .title("데미안").author("헤르만 헤세").build();
        ReflectionTestUtils.setField(work, "id", 10L);
        Quote quote = Quote.builder().userId(1L).work(work)
                .content("새는 알에서...").visibility(Visibility.PRIVATE).build();
        ReflectionTestUtils.setField(quote, "id", 100L);
        QuoteSchedule schedule = QuoteSchedule.builder()
                .userId(1L).quote(quote).sendTime(LocalTime.of(8, 0))
                .repeatType(RepeatType.DAILY).isActive(true).build();
        ReflectionTestUtils.setField(schedule, "id", 1L);
        if (lastSentAt != null) {
            schedule.markSent(lastSentAt);
        }
        return schedule;
    }

    @Test
    @DisplayName("알림을 발송하고 last_sent_at을 갱신한다")
    void dispatchOne_sendsAndMarks() {
        LocalDateTime now = LocalDateTime.of(2026, 8, 28, 8, 0);
        QuoteSchedule s = schedule(null);
        given(scheduleRepository.findById(1L)).willReturn(Optional.of(s));

        executor.dispatchOne(1L, now);

        verify(notificationSender).send(eq(1L), eq("데미안"), eq("새는 알에서..."), eq(100L));
        assertThat(s.getLastSentAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("재조회 시점에 이미 오늘 발송되었으면 중복 발송하지 않는다")
    void dispatchOne_skipsIfAlreadySentToday() {
        LocalDateTime now = LocalDateTime.of(2026, 8, 28, 8, 0);
        QuoteSchedule s = schedule(LocalDateTime.of(2026, 8, 28, 8, 0));
        given(scheduleRepository.findById(1L)).willReturn(Optional.of(s));

        executor.dispatchOne(1L, now);

        verify(notificationSender, never()).send(any(), any(), any(), any());
    }

    @Test
    @DisplayName("스케줄이 사라졌으면 아무 것도 하지 않는다")
    void dispatchOne_scheduleGone() {
        given(scheduleRepository.findById(1L)).willReturn(Optional.empty());

        executor.dispatchOne(1L, LocalDateTime.of(2026, 8, 28, 8, 0));

        verify(notificationSender, never()).send(any(), any(), any(), any());
    }
}
