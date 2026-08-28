package com.galpi.galpibackend.domain.user.dto;

import java.util.List;

public record ProfileResponse(
        Long userId,
        String nickname,
        String bio,
        long followerCount,
        long followingCount,
        boolean isFollowing,
        List<ProfileQuote> quotes
) {

    public record ProfileQuote(
            Long quoteId,
            String content,
            String characterName,
            WorkRef work
    ) {
    }

    // 출처 표기 (저작권) — 공유 대사에는 작품 제목·작가가 반드시 포함
    public record WorkRef(
            String title,
            String author
    ) {
    }
}
