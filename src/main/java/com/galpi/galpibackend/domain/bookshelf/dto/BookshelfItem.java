package com.galpi.galpibackend.domain.bookshelf.dto;

public record BookshelfItem(
        Long workId,
        String title,
        String author,
        String coverUrl,
        long quoteCount
) {
}
