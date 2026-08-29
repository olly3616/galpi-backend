package com.galpi.galpibackend.domain.user.dto;

import com.galpi.galpibackend.domain.work.dto.WorkSource;
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
            WorkSource work    // 출처 — 필수 (저작권)
    ) {
    }
}
