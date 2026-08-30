package com.galpi.galpibackend.domain.schedule.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.galpi.galpibackend.domain.quote.entity.Quote;
import com.galpi.galpibackend.domain.schedule.entity.QuoteSchedule;
import com.galpi.galpibackend.domain.schedule.entity.RepeatType;
import com.galpi.galpibackend.domain.work.dto.WorkBrief;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 내 알림 목록(GET /api/schedules/me)용. 대사 정보를 함께 포함한다.
 */
public record ScheduleWithQuoteResponse(
        Long scheduleId,
        @JsonFormat(pattern = "HH:mm")
        LocalTime sendTime,
        RepeatType repeatType,
        String daysOfWeek,
        LocalDate sendDate,
        boolean isActive,
        QuoteBrief quote
) {

    public record QuoteBrief(
            Long quoteId,
            String content,
            String characterName,
            WorkBrief work
    ) {
    }

    public static ScheduleWithQuoteResponse from(QuoteSchedule schedule) {
        Quote quote = schedule.getQuote();
        QuoteBrief quoteBrief = new QuoteBrief(
                quote.getId(),
                quote.getContent(),
                quote.getCharacterName(),
                WorkBrief.from(quote.getWork())
        );
        return new ScheduleWithQuoteResponse(
                schedule.getId(),
                schedule.getSendTime(),
                schedule.getRepeatType(),
                schedule.getDaysOfWeek(),
                schedule.getSendDate(),
                schedule.isActive(),
                quoteBrief
        );
    }
}
