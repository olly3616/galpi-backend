package com.galpi.galpibackend.domain.image.controller;

import com.galpi.galpibackend.domain.image.dto.ImageUploadResponse;
import com.galpi.galpibackend.domain.image.service.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "이미지", description = "표지 등 이미지 파일 업로드. 반환된 url을 책 추가 시 coverUrl로 사용합니다.")
@RestController
@RequestMapping("/api/images")
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @Operation(summary = "이미지 업로드",
            description = "이미지 파일(jpg/png/webp/gif, 최대 5MB)을 업로드하고 접근 URL을 반환합니다. "
                    + "multipart/form-data의 file 파트로 전송하세요. 반환된 url을 책 추가 시 coverUrl로 넣으면 됩니다.")
    @ApiResponse(responseCode = "201", description = "업로드 성공, 접근 URL 반환")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageUploadResponse> upload(@RequestPart("file") MultipartFile file) {
        String url = imageService.upload(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ImageUploadResponse(url));
    }
}
