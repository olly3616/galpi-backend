package com.galpi.galpibackend.domain.book.dto;

import java.util.List;

public record BookSearchResponse(
        List<BookItem> items,
        int page,
        boolean hasNext
) {

    public record BookItem(
            String title,
            String author,
            String publisher,
            String coverUrl,
            String isbn
    ) {
    }
}
