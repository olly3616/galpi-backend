package com.galpi.galpibackend.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LogoutRequest(
        @Schema(description = "폐기할 refreshToken", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "리프레시 토큰은 필수입니다.")
        String refreshToken
) {
}
