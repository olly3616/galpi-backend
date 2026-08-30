package com.galpi.galpibackend.domain.image.service;

import com.galpi.galpibackend.global.config.AwsProperties;
import com.galpi.galpibackend.global.error.CustomException;
import com.galpi.galpibackend.global.error.ErrorCode;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class ImageService {

    // 허용 이미지 타입 → 저장 확장자
    private static final Map<String, String> ALLOWED_TYPES = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp",
            "image/gif", ".gif"
    );
    private static final String KEY_PREFIX = "covers/";

    private final S3Client s3Client;
    private final AwsProperties awsProperties;

    public ImageService(S3Client s3Client, AwsProperties awsProperties) {
        this.s3Client = s3Client;
        this.awsProperties = awsProperties;
    }

    /**
     * 이미지 파일을 S3에 업로드하고 공개 접근 URL을 반환한다.
     * 파일명은 추측 불가능한 UUID로 생성한다.
     */
    public String upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_IMAGE);
        }
        String contentType = file.getContentType();
        String extension = ALLOWED_TYPES.get(contentType);
        if (extension == null) {
            throw new CustomException(ErrorCode.INVALID_IMAGE);
        }

        String key = KEY_PREFIX + UUID.randomUUID() + extension;
        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(awsProperties.s3().bucket())
                            .key(key)
                            .contentType(contentType)
                            .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (IOException | SdkException e) {
            throw new CustomException(ErrorCode.IMAGE_UPLOAD_FAILED);
        }

        return "https://%s.s3.%s.amazonaws.com/%s".formatted(
                awsProperties.s3().bucket(), awsProperties.region(), key);
    }
}
