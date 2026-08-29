package com.galpi.galpibackend.global.config;

import com.galpi.galpibackend.global.security.CurrentUserId;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    static {
        // @CurrentUserId는 JWT에서 주입되는 값이라 클라이언트 입력이 아니므로 문서에서 숨긴다.
        SpringDocUtils.getConfig().addAnnotationsToIgnore(CurrentUserId.class);
    }

    @Bean
    public OpenAPI galpiOpenAPI() {
        SecurityScheme bearerScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("발급받은 accessToken을 입력하세요. (Bearer 접두사 제외)");

        return new OpenAPI()
                .info(new Info()
                        .title("갈피 API")
                        .description("좋아하는 구절을 담아두는 앱 '갈피'의 백엔드 API 문서")
                        .version("v0.0.1"))
                .components(new Components().addSecuritySchemes(SECURITY_SCHEME_NAME, bearerScheme))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }
}
