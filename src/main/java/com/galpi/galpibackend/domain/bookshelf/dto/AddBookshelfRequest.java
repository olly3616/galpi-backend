package com.galpi.galpibackend.domain.bookshelf.dto;

import com.galpi.galpibackend.domain.work.entity.BookSource;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AddBookshelfRequest(
        @Schema(description = "출처. 검색한 책이면 API, 직접 등록이면 MANUAL", example = "API",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "source는 필수입니다.")
        BookSource source,

        @Schema(description = "책 제목(500자 이하)", example = "데미안", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 500, message = "제목은 500자 이하여야 합니다.")
        String title,

        @Schema(description = "저자(선택, 255자 이하)", example = "헤르만 헤세", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Size(max = 255, message = "저자는 255자 이하여야 합니다.")
        String author,

        @Schema(description = "출판사(선택, 255자 이하)", example = "민음사", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Size(max = 255, message = "출판사는 255자 이하여야 합니다.")
        String publisher,

        @Schema(description = "표지 이미지 URL(선택, 1024자 이하)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Size(max = 1024, message = "표지 이미지 URL은 1024자 이하여야 합니다.")
        String coverUrl,

        @Schema(description = "ISBN(선택, 20자 이하). API 책의 중복 판정 기준", example = "9788937460449",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Size(max = 20, message = "ISBN은 20자 이하여야 합니다.")
        String isbn
) {
}
