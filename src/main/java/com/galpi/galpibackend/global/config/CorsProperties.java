package com.galpi.galpibackend.global.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * CORS 허용 출처. application.yaml의 cors.allowed-origins로 주입한다.
 * 웹 프론트엔드의 실제 도메인을 여기에 추가한다.
 */
@ConfigurationProperties(prefix = "cors")
public record CorsProperties(
        List<String> allowedOrigins
) {
}
