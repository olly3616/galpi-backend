package com.galpi.galpibackend.domain.quote.dto;

import com.galpi.galpibackend.domain.quote.entity.Visibility;

/**
 * 부분 수정(PATCH). null이 아닌 필드만 변경한다.
 */
public record UpdateQuoteRequest(
        String content,
        String memo,
        String characterName,
        Visibility visibility
) {
}
