package com.galpi.galpibackend.domain.quote.dto;

import com.galpi.galpibackend.domain.quote.entity.Quote;
import com.galpi.galpibackend.domain.quote.entity.Visibility;
import com.galpi.galpibackend.domain.work.dto.WorkBrief;

/**
 * 대사 상세/생성 응답. 출처(work)를 함께 포함한다.
 * TODO(F-10): 알림(schedules) 목록 필드 추가 예정.
 */
public record QuoteResponse(
        Long quoteId,
        String content,
        String characterName,
        String memo,
        Visibility visibility,
        WorkBrief work
) {

    public static QuoteResponse from(Quote quote) {
        return new QuoteResponse(
                quote.getId(),
                quote.getContent(),
                quote.getCharacterName(),
                quote.getMemo(),
                quote.getVisibility(),
                WorkBrief.from(quote.getWork())
        );
    }
}
