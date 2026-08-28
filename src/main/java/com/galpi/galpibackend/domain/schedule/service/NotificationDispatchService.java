package com.galpi.galpibackend.domain.schedule.service;

import com.galpi.galpibackend.domain.quote.entity.Quote;
import com.galpi.galpibackend.domain.schedule.entity.QuoteSchedule;
import com.galpi.galpibackend.domain.schedule.entity.RepeatType;
import com.galpi.galpibackend.domain.schedule.notification.NotificationSender;
import com.galpi.galpibackend.domain.schedule.repository.QuoteScheduleRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class NotificationDispatchService {

    private final QuoteScheduleRepository scheduleRepository;
    private final NotificationSender notificationSender;

    public NotificationDispatchService(QuoteScheduleRepository scheduleRepository,
                                       NotificationSender notificationSender) {
        this.scheduleRepository = scheduleRepository;
        this.notificationSender = notificationSender;
    }

    /**
     * 주어진 시각에 발송해야 할 알림을 조회해 발송하고 last_sent_at을 갱신한다.
     * 시각을 인자로 받아 테스트 가능하게 한다.
     */
    @Transactional
    public int dispatchDue(LocalDateTime now) {
        LocalTime sendTime = now.toLocalTime().truncatedTo(ChronoUnit.MINUTES);
        LocalDate today = now.toLocalDate();
        DayOfWeek dayOfWeek = today.getDayOfWeek();

        List<QuoteSchedule> candidates = scheduleRepository.findByIsActiveTrueAndSendTime(sendTime);

        int sent = 0;
        for (QuoteSchedule schedule : candidates) {
            if (alreadySentToday(schedule, today) || !isDue(schedule, dayOfWeek)) {
                continue;
            }
            Quote quote = schedule.getQuote();
            notificationSender.send(
                    schedule.getUserId(),
                    quote.getWork().getTitle(),
                    quote.getContent(),
                    quote.getId());
            schedule.markSent(now);
            sent++;
        }
        return sent;
    }

    private boolean alreadySentToday(QuoteSchedule schedule, LocalDate today) {
        return schedule.getLastSentAt() != null
                && schedule.getLastSentAt().toLocalDate().equals(today);
    }

    boolean isDue(QuoteSchedule schedule, DayOfWeek dayOfWeek) {
        return switch (schedule.getRepeatType()) {
            case DAILY -> true;
            case WEEKLY -> matchesDayOfWeek(schedule.getDaysOfWeek(), dayOfWeek);
            case ONCE -> schedule.getLastSentAt() == null;
        };
    }

    private boolean matchesDayOfWeek(String daysOfWeek, DayOfWeek dayOfWeek) {
        if (!StringUtils.hasText(daysOfWeek)) {
            return false;
        }
        String token = dayOfWeek.name().substring(0, 3); // MONDAY -> MON
        for (String day : daysOfWeek.split(",")) {
            if (day.trim().equalsIgnoreCase(token)) {
                return true;
            }
        }
        return false;
    }
}
