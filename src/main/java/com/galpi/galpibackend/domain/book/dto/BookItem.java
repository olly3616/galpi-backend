package com.galpi.galpibackend.domain.book.dto;

public record BookItem(
        String title,
        String author,
        String publisher,
        String coverUrl,
        String isbn
) {
}
