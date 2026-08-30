package com.galpi.galpibackend.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * FCM(푸시) 설정. enabled=false(기본)면 실제 발송 없이 로깅만 하므로
 * Firebase 자격증명 없이도 앱이 정상 기동한다.
 *
 * @param enabled         true일 때만 실제 FCM 발송 구현을 활성화
 * @param credentialsPath Firebase 서비스 계정 키(JSON) 파일 경로
 */
@ConfigurationProperties(prefix = "fcm")
public record FcmProperties(boolean enabled, String credentialsPath) {
}
