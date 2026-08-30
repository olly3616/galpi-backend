package com.galpi.galpibackend.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {

    /**
     * 자격증명은 지정하지 않는다 → AWS SDK 기본 자격증명 체인이
     * 환경변수(AWS_ACCESS_KEY_ID / AWS_SECRET_ACCESS_KEY)에서 자동으로 읽는다.
     * region만 명시하며, 클라이언트 빌드 시점엔 자격증명을 검증하지 않으므로
     * 키가 없어도 애플리케이션 기동 자체는 정상 진행된다(실제 업로드 시에만 필요).
     */
    @Bean
    public S3Client s3Client(AwsProperties awsProperties) {
        return S3Client.builder()
                .region(Region.of(awsProperties.region()))
                .build();
    }
}
