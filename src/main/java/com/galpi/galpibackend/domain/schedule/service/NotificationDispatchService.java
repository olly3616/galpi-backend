package com.galpi.galpibackend.domain.schedule.service;

import com.galpi.galpibackend.domain.schedule.entity.QuoteSchedule;
import com.galpi.galpibackend.domain.schedule.repository.QuoteScheduleRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class NotificationDispatchService {

    private static final Logger log = LoggerFactory.getLogger(NotificationDispatchService.class);

    private final QuoteScheduleRepository scheduleRepository;
    private final ScheduleDispatchExecutor dispatchExecutor;

    public NotificationDispatchService(QuoteScheduleRepository scheduleRepository,
                                       ScheduleDispatchExecutor dispatchExecutor) {
        this.scheduleRepository = scheduleRepository;
        this.dispatchExecutor = dispatchExecutor;
    }

    /**
     * 주어진 시각에 발송해야 할 알림을 조회해 건별로 발송한다.
     * 발송(외부 I/O)은 건별 독립 트랜잭션(ScheduleDispatchExecutor)에서 처리하므로
     * 한 건이 실패해도 배치 전체가 중단되지 않는다. 시각을 인자로 받아 테스트 가능하게 한다.
     *
     * @return 발송을 시도한(=조건을 만족한) 알림 수
     */
    public int dispatchDue(LocalDateTime now) {
        LocalTime sendTime = now.toLocalTime().truncatedTo(ChronoUnit.MINUTES);
        LocalDate today = now.toLocalDate();
        DayOfWeek dayOfWeek = today.getDayOfWeek();

        List<QuoteSchedule> candidates = scheduleRepository.findActiveBySendTimeWithQuote(sendTime);

        int dispatched = 0;
        for (QuoteSchedule schedule : candidates) {
            if (alreadySentToday(schedule, today) || !isDue(schedule, dayOfWeek)) {
                continue;
            }
            try {
                dispatchExecutor.dispatchOne(schedule.getId(), now);
                dispatched++;
            } catch (Exception e) {
                log.warn("알림 발송 실패 (scheduleId={}): {}", schedule.getId(), e.getMessage());
            }
        }
        return dispatched;
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
