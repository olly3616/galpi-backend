package com.galpi.galpibackend.domain.quote.dto;

import com.galpi.galpibackend.domain.quote.entity.Visibility;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateQuoteRequest(
        @Schema(description = "어느 책에 기록할지(책 ID)", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "workId는 필수입니다.")
        Long workId,

        @Schema(description = "등장인물 이름(선택)", example = "데미안", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String characterName,

        @Schema(description = "대사 본문", example = "새는 알에서 나오려고 투쟁한다.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "대사 본문은 필수입니다.")
        String content,

        @Schema(description = "내 감상 메모(선택)", example = "인생 문장", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String memo,

        @Schema(description = "공개 범위. 미지정 시 PRIVATE", example = "PRIVATE", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Visibility visibility
) {
}
