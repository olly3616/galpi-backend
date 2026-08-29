package com.galpi.galpibackend.domain.feed.dto;

import com.galpi.galpibackend.domain.work.dto.WorkSource;

public record FeedItem(
        Long quoteId,
        String content,
        String characterName,
        Author author,
        WorkSource work,    // 출처 — 필수 (저작권)
        long likeCount,
        boolean isLiked
) {

    public record Author(
            Long userId,
            String nickname
    ) {
    }
}
