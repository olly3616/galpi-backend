package com.galpi.galpibackend.domain.image.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import com.galpi.galpibackend.global.config.AwsProperties;
import com.galpi.galpibackend.global.error.CustomException;
import com.galpi.galpibackend.global.error.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

    @Mock
    private S3Client s3Client;

    private ImageService imageService;

    @BeforeEach
    void setUp() {
        AwsProperties props = new AwsProperties("ap-northeast-2", new AwsProperties.S3("test-bucket"));
        imageService = new ImageService(s3Client, props);
    }

    @Test
    @DisplayName("이미지를 업로드하면 S3에 저장하고 공개 URL을 반환한다")
    void upload_success() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "cover.jpg", "image/jpeg", "fake-image-bytes".getBytes());

        String url = imageService.upload(file);

        assertThat(url).startsWith("https://test-bucket.s3.ap-northeast-2.amazonaws.com/covers/");
        assertThat(url).endsWith(".jpg");
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    @DisplayName("이미지가 아닌 파일이면 INVALID_IMAGE 예외를 던진다")
    void upload_invalidType() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "doc.pdf", "application/pdf", "not-an-image".getBytes());

        assertThatThrownBy(() -> imageService.upload(file))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_IMAGE);
    }

    @Test
    @DisplayName("빈 파일이면 INVALID_IMAGE 예외를 던진다")
    void upload_emptyFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "empty.jpg", "image/jpeg", new byte[0]);

        assertThatThrownBy(() -> imageService.upload(file))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_IMAGE);
    }
}
