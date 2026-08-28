package com.galpi.galpibackend.domain.quote.dto;

import com.galpi.galpibackend.domain.quote.entity.Visibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateQuoteRequest(
        @NotNull(message = "workId는 필수입니다.")
        Long workId,

        String characterName,

        @NotBlank(message = "대사 본문은 필수입니다.")
        String content,

        String memo,

        // 미지정 시 PRIVATE로 저장
        Visibility visibility
) {
}
