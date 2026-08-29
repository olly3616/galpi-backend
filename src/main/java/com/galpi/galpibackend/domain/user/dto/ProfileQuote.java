package com.galpi.galpibackend.domain.user.dto;

import com.galpi.galpibackend.domain.work.dto.WorkSource;

public record ProfileQuote(
        Long quoteId,
        String content,
        String characterName,
        WorkSource work    // 출처 — 필수 (저작권)
) {
}
