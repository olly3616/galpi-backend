package com.galpi.galpibackend.domain.bookshelf.dto;

import com.galpi.galpibackend.domain.work.entity.BookSource;
import com.galpi.galpibackend.domain.work.entity.BookType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddBookshelfRequest(
        @NotNull(message = "source는 필수입니다.")
        BookSource source,

        @NotBlank(message = "제목은 필수입니다.")
        String title,

        String author,

        String publisher,

        String coverUrl,

        String isbn,

        @NotNull(message = "type은 필수입니다.")
        BookType type
) {
}
