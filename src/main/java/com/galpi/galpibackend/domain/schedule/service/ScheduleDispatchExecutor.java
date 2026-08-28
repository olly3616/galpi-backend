package com.galpi.galpibackend.domain.schedule.service;

import com.galpi.galpibackend.domain.quote.entity.Quote;
import com.galpi.galpibackend.domain.schedule.entity.QuoteSchedule;
import com.galpi.galpibackend.domain.schedule.notification.NotificationSender;
import com.galpi.galpibackend.domain.schedule.repository.QuoteScheduleRepository;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 알림 한 건을 독립 트랜잭션으로 발송/기록한다.
 * 배치 전체를 한 트랜잭션으로 묶지 않아, 한 건이 실패해도 나머지 발송과 이미 기록된 last_sent_at이 보존된다.
 */
@Component
public class ScheduleDispatchExecutor {

    private final QuoteScheduleRepository scheduleRepository;
    private final NotificationSender notificationSender;

    public ScheduleDispatchExecutor(QuoteScheduleRepository scheduleRepository,
                                    NotificationSender notificationSender) {
        this.scheduleRepository = scheduleRepository;
        this.notificationSender = notificationSender;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void dispatchOne(Long scheduleId, LocalDateTime now) {
        QuoteSchedule schedule = scheduleRepository.findById(scheduleId).orElse(null);
        if (schedule == null) {
            return;
        }
        // 재조회 시점에 이미 오늘 발송되었으면(동시 실행 등) 중복 발송하지 않는다.
        if (schedule.getLastSentAt() != null
                && schedule.getLastSentAt().toLocalDate().equals(now.toLocalDate())) {
            return;
        }
        Quote quote = schedule.getQuote();
        notificationSender.send(
                schedule.getUserId(),
                quote.getWork().getTitle(),
                quote.getContent(),
                quote.getId());
        schedule.markSent(now);
    }
}
