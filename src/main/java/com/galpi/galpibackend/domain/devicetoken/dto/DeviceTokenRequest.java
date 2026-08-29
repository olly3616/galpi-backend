package com.galpi.galpibackend.domain.devicetoken.dto;

import com.galpi.galpibackend.domain.devicetoken.entity.Platform;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DeviceTokenRequest(
        @Schema(description = "FCM 디바이스 토큰", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "FCM 토큰은 필수입니다.")
        String token,

        @Schema(description = "플랫폼", example = "ANDROID", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "플랫폼은 필수입니다.")
        Platform platform
) {
}
