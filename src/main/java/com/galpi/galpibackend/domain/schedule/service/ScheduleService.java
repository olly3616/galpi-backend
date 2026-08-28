package com.galpi.galpibackend.domain.schedule.service;

import com.galpi.galpibackend.domain.quote.entity.Quote;
import com.galpi.galpibackend.domain.quote.repository.QuoteRepository;
import com.galpi.galpibackend.domain.schedule.dto.CreateScheduleRequest;
import com.galpi.galpibackend.domain.schedule.dto.DeleteScheduleResponse;
import com.galpi.galpibackend.domain.schedule.dto.ScheduleResponse;
import com.galpi.galpibackend.domain.schedule.dto.ScheduleWithQuoteResponse;
import com.galpi.galpibackend.domain.schedule.dto.UpdateScheduleRequest;
import com.galpi.galpibackend.domain.schedule.entity.QuoteSchedule;
import com.galpi.galpibackend.domain.schedule.entity.RepeatType;
import com.galpi.galpibackend.domain.schedule.repository.QuoteScheduleRepository;
import com.galpi.galpibackend.global.error.CustomException;
import com.galpi.galpibackend.global.error.ErrorCode;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ScheduleService {

    private final QuoteScheduleRepository scheduleRepository;
    private final QuoteRepository quoteRepository;

    public ScheduleService(QuoteScheduleRepository scheduleRepository, QuoteRepository quoteRepository) {
        this.scheduleRepository = scheduleRepository;
        this.quoteRepository = quoteRepository;
    }

    @Transactional
    public ScheduleResponse createSchedule(Long userId, Long quoteId, CreateScheduleRequest request) {
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
        if (!quote.isOwnedBy(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
        validateWeekly(request.repeatType(), request.daysOfWeek());

        QuoteSchedule schedule = QuoteSchedule.builder()
                .userId(userId)
                .quote(quote)
                .sendTime(normalizeToMinute(request.sendTime()))
                .repeatType(request.repeatType())
                .daysOfWeek(request.daysOfWeek())
                .isActive(true)
                .build();
        scheduleRepository.save(schedule);

        return ScheduleResponse.from(schedule);
    }

    @Transactional(readOnly = true)
    public List<ScheduleWithQuoteResponse> getMySchedules(Long userId) {
        return scheduleRepository.findByUserIdWithQuote(userId).stream()
                .map(ScheduleWithQuoteResponse::from)
                .toList();
    }

    @Transactional
    public ScheduleResponse updateSchedule(Long userId, Long scheduleId, UpdateScheduleRequest request) {
        QuoteSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
        if (!schedule.isOwnedBy(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        // 변경 후 최종 상태 기준으로 WEEKLY 유효성 검사
        RepeatType effectiveType = request.repeatType() != null ? request.repeatType() : schedule.getRepeatType();
        String effectiveDays = request.daysOfWeek() != null ? request.daysOfWeek() : schedule.getDaysOfWeek();
        validateWeekly(effectiveType, effectiveDays);

        LocalTime sendTime = request.sendTime() != null ? normalizeToMinute(request.sendTime()) : null;
        schedule.update(sendTime, request.repeatType(), request.daysOfWeek(), request.isActive());

        return ScheduleResponse.from(schedule);
    }

    @Transactional
    public DeleteScheduleResponse deleteSchedule(Long userId, Long scheduleId) {
        QuoteSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
        if (!schedule.isOwnedBy(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
        scheduleRepository.delete(schedule);
        return new DeleteScheduleResponse(true);
    }

    private static final Set<String> VALID_DAYS = Set.of("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN");

    private void validateWeekly(RepeatType repeatType, String daysOfWeek) {
        if (repeatType != RepeatType.WEEKLY) {
            return;
        }
        if (!StringUtils.hasText(daysOfWeek)) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR, "WEEKLY 반복은 daysOfWeek가 필요합니다.");
        }
        for (String day : daysOfWeek.split(",")) {
            if (!VALID_DAYS.contains(day.trim().toUpperCase())) {
                throw new CustomException(ErrorCode.VALIDATION_ERROR,
                        "daysOfWeek는 MON,TUE,WED,THU,FRI,SAT,SUN 중 콤마로 구분해 지정해야 합니다.");
            }
        }
    }

    private LocalTime normalizeToMinute(LocalTime time) {
        return time.truncatedTo(ChronoUnit.MINUTES);
    }
}
