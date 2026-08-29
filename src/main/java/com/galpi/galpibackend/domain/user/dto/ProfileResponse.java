package com.galpi.galpibackend.domain.user.dto;

import com.galpi.galpibackend.global.web.PageResponse;

public record ProfileResponse(
        Long userId,
        String nickname,
        String bio,
        long followerCount,
        long followingCount,
        boolean isFollowing,
        PageResponse<ProfileQuote> quotes
) {
}
