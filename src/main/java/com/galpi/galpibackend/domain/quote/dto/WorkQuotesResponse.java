package com.galpi.galpibackend.domain.quote.dto;

import com.galpi.galpibackend.domain.quote.entity.Quote;
import com.galpi.galpibackend.domain.quote.entity.Visibility;
import com.galpi.galpibackend.domain.work.dto.WorkBrief;
import com.galpi.galpibackend.global.web.PageResponse;

/**
 * 책 상세 = 대사 모아보기 (F-07) 응답. 출처(work) + 페이지네이션된 대사 목록.
 */
public record WorkQuotesResponse(
        WorkBrief work,
        PageResponse<QuoteSummary> quotes
) {

    public record QuoteSummary(
            Long quoteId,
            String characterName,
            String content,
            String memo,
            boolean hasSchedule,
            Visibility visibility
    ) {
        public static QuoteSummary from(Quote quote, boolean hasSchedule) {
            return new QuoteSummary(
                    quote.getId(),
                    quote.getCharacterName(),
                    quote.getContent(),
                    quote.getMemo(),
                    hasSchedule,
                    quote.getVisibility()
            );
        }
    }
}
