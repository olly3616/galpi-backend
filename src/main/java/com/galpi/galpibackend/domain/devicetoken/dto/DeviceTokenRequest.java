package com.galpi.galpibackend.domain.devicetoken.dto;

import com.galpi.galpibackend.domain.devicetoken.entity.Platform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DeviceTokenRequest(
        @NotBlank(message = "FCM 토큰은 필수입니다.")
        String token,

        @NotNull(message = "플랫폼은 필수입니다.")
        Platform platform
) {
}
