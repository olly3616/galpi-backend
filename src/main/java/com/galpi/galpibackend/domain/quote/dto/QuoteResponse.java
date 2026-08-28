package com.galpi.galpibackend.domain.quote.dto;

import com.galpi.galpibackend.domain.quote.entity.Quote;
import com.galpi.galpibackend.domain.quote.entity.Visibility;
import com.galpi.galpibackend.domain.schedule.dto.ScheduleResponse;
import com.galpi.galpibackend.domain.work.dto.WorkBrief;
import java.util.List;

/**
 * 대사 상세/생성 응답. 출처(work)와 알림(schedules)을 함께 포함한다.
 */
public record QuoteResponse(
        Long quoteId,
        String content,
        String characterName,
        String memo,
        Visibility visibility,
        WorkBrief work,
        List<ScheduleResponse> schedules
) {

    public static QuoteResponse from(Quote quote) {
        return of(quote, List.of());
    }

    public static QuoteResponse of(Quote quote, List<ScheduleResponse> schedules) {
        return new QuoteResponse(
                quote.getId(),
                quote.getContent(),
                quote.getCharacterName(),
                quote.getMemo(),
                quote.getVisibility(),
                WorkBrief.from(quote.getWork()),
                schedules
        );
    }
}
