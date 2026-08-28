package com.galpi.galpibackend.domain.bookshelf.dto;

import java.util.List;

public record BookshelfResponse(
        List<BookshelfItem> items,
        int page,
        boolean hasNext
) {

    public record BookshelfItem(
            Long workId,
            String title,
            String author,
            String coverUrl,
            long quoteCount
    ) {
    }
}
