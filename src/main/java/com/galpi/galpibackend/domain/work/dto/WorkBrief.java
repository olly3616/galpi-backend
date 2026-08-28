package com.galpi.galpibackend.domain.work.dto;

import com.galpi.galpibackend.domain.work.entity.Work;

public record WorkBrief(
        Long workId,
        String title,
        String author,
        String coverUrl
) {

    public static WorkBrief from(Work work) {
        return new WorkBrief(work.getId(), work.getTitle(), work.getAuthor(), work.getCoverUrl());
    }
}
