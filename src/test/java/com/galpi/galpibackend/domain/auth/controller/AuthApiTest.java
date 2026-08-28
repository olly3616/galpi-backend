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
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthApiTest {

    @Autowired
    private MockMvc mockMvc;

    private String signupBody(String email, String nickname) {
        return """
                { "email": "%s", "password": "password123", "nickname": "%s" }
                """.formatted(email, nickname);
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
}
