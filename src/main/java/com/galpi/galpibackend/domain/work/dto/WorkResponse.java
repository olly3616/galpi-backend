package com.galpi.galpibackend.domain.work.dto;

import com.galpi.galpibackend.domain.work.entity.BookSource;
import com.galpi.galpibackend.domain.work.entity.BookType;
import com.galpi.galpibackend.domain.work.entity.Work;

public record WorkResponse(
        Long workId,
        String title,
        String author,
        String coverUrl,
        BookType type,
        BookSource source
) {

    public static WorkResponse from(Work work) {
        return new WorkResponse(
                work.getId(),
                work.getTitle(),
                work.getAuthor(),
                work.getCoverUrl(),
                work.getType(),
                work.getSource()
        );
    }
}
