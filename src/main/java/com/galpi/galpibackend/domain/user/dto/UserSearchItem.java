package com.galpi.galpibackend.domain.user.dto;

public record UserSearchItem(
        Long userId,
        String nickname,
        String bio,
        boolean isFollowing
) {
}
