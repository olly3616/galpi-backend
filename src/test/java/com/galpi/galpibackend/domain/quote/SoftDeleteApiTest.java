package com.galpi.galpibackend.domain.quote;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SoftDeleteApiTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtProvider jwtProvider;
    @Autowired private UserRepository userRepository;
    @Autowired private WorkRepository workRepository;
    @Autowired private QuoteRepository quoteRepository;
    @PersistenceContext private EntityManager em;

    @Test
    @DisplayName("대사 삭제는 소프트 딜리트 — 조회는 404지만 DB 행은 남고 deleted_at이 채워진다")
    void deleteQuote_isSoftDelete() throws Exception {
        User user = userRepository.save(User.builder()
                .email("soft@galpi.com").password("h").nickname("소프트").build());
        Work work = workRepository.save(Work.builder().source(BookSource.API)
                .title("데미안").author("헤르만 헤세").isbn("9788937460449").build());
        Quote quote = quoteRepository.save(Quote.builder().userId(user.getId()).work(work)
                .content("새는 알에서...").visibility(Visibility.PRIVATE).build());
        Long quoteId = quote.getId();
        String token = jwtProvider.createAccessToken(user.getId());

        // 삭제
        mockMvc.perform(delete("/api/quotes/" + quoteId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 조회하면 없는 것으로 취급 (NOT_FOUND)
        mockMvc.perform(get("/api/quotes/" + quoteId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());

        // JPA로도 조회되지 않는다 (@SQLRestriction)
        assertThat(quoteRepository.findById(quoteId)).isEmpty();

        // 그러나 DB에는 행이 남아 있고(=하드 삭제 아님) deleted_at이 채워져 있다
        Number total = (Number) em.createNativeQuery(
                "select count(*) from quotes where id = :id").setParameter("id", quoteId).getSingleResult();
        assertThat(total.longValue()).isEqualTo(1L);

        Number softDeleted = (Number) em.createNativeQuery(
                "select count(*) from quotes where id = :id and deleted_at is not null")
                .setParameter("id", quoteId).getSingleResult();
        assertThat(softDeleted.longValue()).isEqualTo(1L);
    }
}
