package com.galpi.galpibackend.domain.quote.dto;

import com.galpi.galpibackend.domain.quote.entity.Visibility;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 부분 수정(PATCH). null이 아닌 필드만 변경한다. (모든 필드 선택)
 */
public record UpdateQuoteRequest(
        @Schema(description = "대사 본문(선택)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String content,

        @Schema(description = "메모(선택)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String memo,

        @Schema(description = "등장인물(선택)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String characterName,

        @Schema(description = "공개 범위(선택)", example = "FOLLOWERS", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Visibility visibility
) {
}
