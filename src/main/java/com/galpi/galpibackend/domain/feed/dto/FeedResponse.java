package com.galpi.galpibackend.domain.feed.dto;

import java.util.List;

public record FeedResponse(
        List<FeedItem> items,
        int page,
        boolean hasNext
) {

    public record FeedItem(
            Long quoteId,
            String content,
            String characterName,
            Author author,
            WorkRef work,       // 출처 — 필수 (저작권)
            long likeCount,
            boolean isLiked
    ) {
    }

    public record Author(
            Long userId,
            String nickname
    ) {
    }

    public record WorkRef(
            String title,
            String author
    ) {
    }
}
