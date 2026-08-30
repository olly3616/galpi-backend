package com.galpi.galpibackend.domain.quote.dto;

import com.galpi.galpibackend.domain.quote.entity.Visibility;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

/**
 * 부분 수정(PATCH). null이 아닌 필드만 변경한다. (모든 필드 선택)
 */
public record UpdateQuoteRequest(
        @Schema(description = "대사 본문(선택, 5000자 이하)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Size(max = 5000, message = "대사 본문은 5000자 이하여야 합니다.")
        String content,

        @Schema(description = "메모(선택, 2000자 이하)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Size(max = 2000, message = "메모는 2000자 이하여야 합니다.")
        String memo,

        @Schema(description = "등장인물(선택, 100자 이하)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Size(max = 100, message = "등장인물 이름은 100자 이하여야 합니다.")
        String characterName,

        @Schema(description = "공개 범위(선택)", example = "FOLLOWERS", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Visibility visibility
) {
}
