package com.galpi.galpibackend.domain.devicetoken.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.galpi.galpibackend.domain.devicetoken.repository.DeviceTokenRepository;
import com.galpi.galpibackend.global.jwt.JwtProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class DeviceTokenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private DeviceTokenRepository deviceTokenRepository;

    private static final String BODY = """
            { "token": "fcm-token-integration", "platform": "ANDROID" }
            """;

    @Test
    @DisplayName("인증 토큰 없이 요청하면 401을 반환한다")
    void register_withoutToken_unauthorized() throws Exception {
        mockMvc.perform(post("/api/device-tokens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("유효한 액세스 토큰으로 요청하면 토큰이 저장되고 200을 반환한다")
    void register_withToken_success() throws Exception {
        String accessToken = jwtProvider.createAccessToken(1L);

        mockMvc.perform(post("/api/device-tokens")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        assertThat(deviceTokenRepository.findByToken("fcm-token-integration")).isPresent();
    }
}
