package com.galpi.galpibackend.domain.image.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ImageUploadResponse(
        @Schema(description = "업로드된 이미지의 접근 URL. 책 추가 시 coverUrl로 사용하세요.",
                example = "https://galpi-cover-images.s3.ap-northeast-2.amazonaws.com/covers/1a2b3c.jpg")
        String url
) {
}
