package com.galpi.galpibackend.domain.auth.dto;

public record RefreshResponse(
        String accessToken
) {

    public static RefreshResponse of(String accessToken) {
        return new RefreshResponse(accessToken);
    }
}
