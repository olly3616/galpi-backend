package com.galpi.galpibackend.domain.schedule.service;

import com.galpi.galpibackend.domain.quote.entity.Quote;
import com.galpi.galpibackend.domain.quote.repository.QuoteRepository;
import com.galpi.galpibackend.domain.schedule.dto.CreateScheduleRequest;
import com.galpi.galpibackend.domain.schedule.dto.ScheduleResponse;
import com.galpi.galpibackend.domain.schedule.dto.ScheduleWithQuoteResponse;
import com.galpi.galpibackend.domain.schedule.dto.UpdateScheduleRequest;
import com.galpi.galpibackend.domain.schedule.entity.QuoteSchedule;
import com.galpi.galpibackend.domain.schedule.entity.RepeatType;
import com.galpi.galpibackend.domain.schedule.repository.QuoteScheduleRepository;
import com.galpi.galpibackend.global.error.CustomException;
import com.galpi.galpibackend.global.error.ErrorCode;
import com.galpi.galpibackend.global.web.PageResponse;
import com.galpi.galpibackend.global.web.SuccessResponse;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
        validateOnce(request.repeatType(), request.sendDate(), request.sendDate());

        QuoteSchedule schedule = QuoteSchedule.builder()
                .userId(userId)
                .quote(quote)
                .sendTime(normalizeToMinute(request.sendTime()))
                .repeatType(request.repeatType())
                .daysOfWeek(request.daysOfWeek())
                .sendDate(request.sendDate())
                .isActive(true)
                .build();
        scheduleRepository.save(schedule);

        return ScheduleResponse.from(schedule);
    }

    @Transactional(readOnly = true)
    public PageResponse<ScheduleWithQuoteResponse> getMySchedules(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<QuoteSchedule> schedulePage = scheduleRepository.findByUserIdWithQuote(userId, pageable);
        List<ScheduleWithQuoteResponse> items = schedulePage.getContent().stream()
                .map(ScheduleWithQuoteResponse::from)
                .toList();
        return PageResponse.from(schedulePage, items);
    }

    @Transactional
    public ScheduleResponse updateSchedule(Long userId, Long scheduleId, UpdateScheduleRequest request) {
        QuoteSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
        if (!schedule.isOwnedBy(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        // 변경 후 최종 상태 기준으로 WEEKLY/ONCE 유효성 검사
        RepeatType effectiveType = request.repeatType() != null ? request.repeatType() : schedule.getRepeatType();
        String effectiveDays = request.daysOfWeek() != null ? request.daysOfWeek() : schedule.getDaysOfWeek();
        LocalDate effectiveDate = request.sendDate() != null ? request.sendDate() : schedule.getSendDate();
        validateWeekly(effectiveType, effectiveDays);
        validateOnce(effectiveType, effectiveDate, request.sendDate());

        LocalTime sendTime = request.sendTime() != null ? normalizeToMinute(request.sendTime()) : null;
        schedule.update(sendTime, request.repeatType(), request.daysOfWeek(), request.sendDate(), request.isActive());

        return ScheduleResponse.from(schedule);
    }

    @Transactional
    public SuccessResponse deleteSchedule(Long userId, Long scheduleId) {
        QuoteSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
        if (!schedule.isOwnedBy(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
        scheduleRepository.delete(schedule);
        return SuccessResponse.ok();
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

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    /**
     * ONCE 반복은 발송 날짜(sendDate)가 필요하다. 날짜가 이번 요청으로 새로 지정된 경우
     * 과거 날짜는 거부한다(이미 저장된 과거 날짜를 그대로 두는 수정은 막지 않는다).
     */
    private void validateOnce(RepeatType effectiveType, LocalDate effectiveDate, LocalDate requestedDate) {
        if (effectiveType != RepeatType.ONCE) {
            return;
        }
        if (effectiveDate == null) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR, "ONCE 반복은 sendDate가 필요합니다.");
        }
        if (requestedDate != null && requestedDate.isBefore(LocalDate.now(KST))) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR, "sendDate는 오늘 이후여야 합니다.");
        }
    }

    private LocalTime normalizeToMinute(LocalTime time) {
        return time.truncatedTo(ChronoUnit.MINUTES);
    }
}
