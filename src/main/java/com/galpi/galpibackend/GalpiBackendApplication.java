package com.galpi.galpibackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

// JWT 무상태 인증만 사용하므로, 쓰지 않는 기본 인메모리 유저/랜덤 비밀번호 자동설정을 제외한다.
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@ConfigurationPropertiesScan
public class GalpiBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(GalpiBackendApplication.class, args);
    }

}