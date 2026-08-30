package com.galpi.galpibackend.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

/**
 * 내 프로필 부분 수정(PATCH). null이 아닌 필드만 변경한다. (모든 필드 선택)
 */
public record UpdateProfileRequest(
        @Schema(description = "닉네임 (2~20자, 중복 불가) — 선택", example = "책읽는너구리",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Size(min = 2, max = 20, message = "닉네임은 2~20자여야 합니다.")
        String nickname,

        @Schema(description = "한 줄 소개 (선택). 빈 문자열이면 소개를 비웁니다.",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Size(max = 500, message = "소개는 500자 이하여야 합니다.")
        String bio,

        @Schema(description = "프로필 이미지 URL (선택). /api/images로 업로드해 받은 url을 넣습니다.",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String profileImageUrl
) {
}
