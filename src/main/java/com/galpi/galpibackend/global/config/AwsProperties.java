package com.galpi.galpibackend.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AWS 관련 설정. region과 S3 버킷명은 환경변수로 주입한다.
 * 자격증명(AccessKey/SecretKey)은 AWS SDK 기본 체인이 환경변수
 * (AWS_ACCESS_KEY_ID / AWS_SECRET_ACCESS_KEY)에서 자동으로 읽는다.
 */
@ConfigurationProperties(prefix = "aws")
public record AwsProperties(String region, S3 s3) {

    public record S3(String bucket) {
    }
}
