package com.galpi.galpibackend.global.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.galpi.galpibackend.domain.quote.entity.Quote;
import com.galpi.galpibackend.domain.quote.entity.Visibility;
import com.galpi.galpibackend.domain.quote.repository.QuoteRepository;
import com.galpi.galpibackend.domain.user.entity.User;
import com.galpi.galpibackend.domain.user.repository.UserRepository;
import com.galpi.galpibackend.domain.work.entity.BookSource;
import com.galpi.galpibackend.domain.work.entity.Work;
import com.galpi.galpibackend.domain.work.repository.WorkRepository;
import com.galpi.galpibackend.global.jwt.JwtProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthorizationApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkRepository workRepository;

    @Autowired
    private QuoteRepository quoteRepository;

    private User saveUser(String email, String nickname) {
        return userRepository.save(User.builder()
                .email(email).password("hashed").nickname(nickname).build());
    }

    @Test
    @DisplayName("인증 토큰 없이 보호된 API 호출 시 401과 UNAUTHORIZED를 반환한다")
    void protectedEndpoint_noToken_401() throws Exception {
        mockMvc.perform(get("/api/bookshelf/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("리프레시 토큰을 액세스 토큰으로 사용하면 401을 반환한다")
    void refreshTokenAsAccess_401() throws Exception {
        User user = saveUser("refresh@galpi.com", "리프레시");
        String refreshToken = jwtProvider.createRefreshToken(user.getId());

        mockMvc.perform(get("/api/bookshelf/me")
                        .header("Authorization", "Bearer " + refreshToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("유효한 액세스 토큰으로 보호된 API를 호출하면 200을 반환한다")
    void validAccessToken_200() throws Exception {
        User user = saveUser("valid@galpi.com", "유효유저");
        String accessToken = jwtProvider.createAccessToken(user.getId());

        mockMvc.perform(get("/api/bookshelf/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("페이지 파라미터가 음수면 500이 아니라 400과 VALIDATION_ERROR를 반환한다")
    void negativePage_400() throws Exception {
        User user = saveUser("paging@galpi.com", "페이징");
        String accessToken = jwtProvider.createAccessToken(user.getId());

        mockMvc.perform(get("/api/bookshelf/me?page=-1")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("남의 대사를 조회하면 403과 FORBIDDEN을 반환한다")
    void othersQuote_403() throws Exception {
        User owner = saveUser("owner@galpi.com", "주인");
        User other = saveUser("other@galpi.com", "타인");

        Work work = workRepository.save(Work.builder()
                .source(BookSource.API)
                .title("데미안").author("헤르만 헤세").build());
        Quote quote = quoteRepository.save(Quote.builder()
                .userId(owner.getId()).work(work)
                .content("새는 알에서...").visibility(Visibility.PRIVATE).build());

        String otherToken = jwtProvider.createAccessToken(other.getId());

        mockMvc.perform(get("/api/quotes/" + quote.getId())
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
    }
}
