package com.galpi.galpibackend.domain.bookshelf.dto;

public record AddBookshelfResponse(
        Long workId,
        boolean addedToShelf
) {
}
