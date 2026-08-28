package com.galpi.galpibackend.global.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

/**
 * 인증된 사용자의 userId(Long)를 컨트롤러 파라미터로 주입한다.
 * JwtAuthenticationFilter가 principal에 userId를 넣어두므로 그대로 꺼낸다.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@AuthenticationPrincipal
public @interface CurrentUserId {
}
