package com.galpi.galpibackend.domain.auth.dto;

import com.galpi.galpibackend.domain.user.entity.User;

public record AuthResponse(
        Long userId,
        String email,
        String nickname,
        String accessToken,
        String refreshToken
) {

    public static AuthResponse of(User user, String accessToken, String refreshToken) {
        return new AuthResponse(user.getId(), user.getEmail(), user.getNickname(), accessToken, refreshToken);
    }
}
