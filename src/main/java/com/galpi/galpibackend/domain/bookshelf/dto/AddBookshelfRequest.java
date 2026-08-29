package com.galpi.galpibackend.domain.bookshelf.dto;

import com.galpi.galpibackend.domain.work.entity.BookSource;
import com.galpi.galpibackend.domain.work.entity.BookType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddBookshelfRequest(
        @Schema(description = "출처. 검색한 책이면 API, 직접 등록이면 MANUAL", example = "API",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "source는 필수입니다.")
        BookSource source,

        @Schema(description = "책 제목", example = "데미안", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "제목은 필수입니다.")
        String title,

        @Schema(description = "저자(선택)", example = "헤르만 헤세", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String author,

        @Schema(description = "출판사(선택)", example = "민음사", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String publisher,

        @Schema(description = "표지 이미지 URL(선택)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String coverUrl,

        @Schema(description = "ISBN(선택). API 책의 중복 판정 기준", example = "9788937460449",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String isbn,

        @Schema(description = "유형. 소설(NOVEL) 또는 웹소설(WEBNOVEL)", example = "NOVEL",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "type은 필수입니다.")
        BookType type
) {
}
