package com.galpi.galpibackend.domain.auth.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Map;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String signupBody(String email, String nickname) {
        return """
                { "email": "%s", "password": "password123", "nickname": "%s" }
                """.formatted(email, nickname);
    }

    private String signupAndGetRefreshToken(String email, String nickname) throws Exception {
        String body = mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupBody(email, nickname)))
                .andReturn().getResponse().getContentAsString();
        Map<?, ?> json = objectMapper.readValue(body, Map.class);
        return (String) json.get("refreshToken");
    }

    private String refreshBody(String refreshToken) {
        return """
                { "refreshToken": "%s" }
                """.formatted(refreshToken);
    }

    @Test
    @DisplayName("회원가입 성공 시 201과 토큰을 반환한다")
    void signup_success() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupBody("a@galpi.com", "책벌레")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nickname").value("책벌레"))
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    @Test
    @DisplayName("이메일이 중복되면 409와 EMAIL_DUPLICATED를 반환한다")
    void signup_duplicateEmail() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(signupBody("dup@galpi.com", "닉네임A")));

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupBody("dup@galpi.com", "닉네임B")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("EMAIL_DUPLICATED"));
    }

    @Test
    @DisplayName("비밀번호가 짧으면 400과 VALIDATION_ERROR를 반환한다")
    void signup_validationError() throws Exception {
        String body = """
                { "email": "short@galpi.com", "password": "123", "nickname": "짧은비번" }
                """;
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("로그인 성공 시 200과 토큰을 반환한다")
    void login_success() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(signupBody("login@galpi.com", "로그인유저")));

        String loginBody = """
                { "email": "login@galpi.com", "password": "password123" }
                """;
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists());
    }

    @Test
    @DisplayName("비밀번호가 틀리면 401과 INVALID_CREDENTIALS를 반환한다")
    void login_wrongPassword() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(signupBody("wrong@galpi.com", "틀린비번")));

        String loginBody = """
                { "email": "wrong@galpi.com", "password": "wrongpassword" }
                """;
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    @DisplayName("토큰 갱신 시 새 토큰 쌍을 발급하고, 사용된 리프레시 토큰은 더 이상 통하지 않는다(회전)")
    void refresh_rotatesToken() throws Exception {
        String refreshToken = signupAndGetRefreshToken("rotate@galpi.com", "회전유저");

        // 첫 갱신: 새 accessToken/refreshToken 발급
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshBody(refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());

        // 이미 회전(폐기)된 이전 리프레시 토큰은 거부된다
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshBody(refreshToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("INVALID_REFRESH_TOKEN"));
    }

    @Test
    @DisplayName("로그아웃하면 해당 리프레시 토큰으로 더 이상 갱신할 수 없다")
    void logout_revokesRefreshToken() throws Exception {
        String refreshToken = signupAndGetRefreshToken("logout@galpi.com", "로그아웃유저");

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshBody(refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshBody(refreshToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("INVALID_REFRESH_TOKEN"));
    }
}
