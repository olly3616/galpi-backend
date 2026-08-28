package com.galpi.galpibackend.domain.user.dto;

import java.util.List;

public record UserSearchResponse(
        List<UserSearchItem> items
) {

    public record UserSearchItem(
            Long userId,
            String nickname,
            String bio,
            boolean isFollowing
    ) {
    }
}
