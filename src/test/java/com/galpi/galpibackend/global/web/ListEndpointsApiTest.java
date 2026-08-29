package com.galpi.galpibackend.global.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.galpi.galpibackend.domain.follow.entity.Follow;
import com.galpi.galpibackend.domain.follow.repository.FollowRepository;
import com.galpi.galpibackend.domain.quote.entity.Quote;
import com.galpi.galpibackend.domain.quote.entity.Visibility;
import com.galpi.galpibackend.domain.quote.repository.QuoteRepository;
import com.galpi.galpibackend.domain.schedule.entity.QuoteSchedule;
import com.galpi.galpibackend.domain.schedule.entity.RepeatType;
import com.galpi.galpibackend.domain.schedule.repository.QuoteScheduleRepository;
import com.galpi.galpibackend.domain.user.entity.User;
import com.galpi.galpibackend.domain.user.repository.UserRepository;
import com.galpi.galpibackend.domain.work.entity.BookSource;
import com.galpi.galpibackend.domain.work.entity.BookType;
import com.galpi.galpibackend.domain.work.entity.Work;
import com.galpi.galpibackend.domain.work.repository.WorkRepository;
import com.galpi.galpibackend.global.jwt.JwtProvider;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * 페이지네이션 리스트 엔드포인트가 {items,page,hasNext} 형태로 응답하고,
 * 새로 도입한 fetch-join + 페이지네이션 쿼리가 런타임에 정상 실행되는지 검증한다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ListEndpointsApiTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtProvider jwtProvider;
    @Autowired private UserRepository userRepository;
    @Autowired private WorkRepository workRepository;
    @Autowired private QuoteRepository quoteRepository;
    @Autowired private QuoteScheduleRepository scheduleRepository;
    @Autowired private FollowRepository followRepository;

    private User saveUser(String email, String nickname) {
        return userRepository.save(User.builder().email(email).password("h").nickname(nickname).build());
    }

    private Work saveWork() {
        return workRepository.save(Work.builder().source(BookSource.API).type(BookType.NOVEL)
                .title("데미안").author("헤르만 헤세").isbn("9788937460449").build());
    }

    private Quote saveQuote(Long userId, Work work, Visibility visibility) {
        return quoteRepository.save(Quote.builder().userId(userId).work(work)
                .content("새는 알에서...").visibility(visibility).build());
    }

    @Test
    @DisplayName("내 알림 목록은 fetch-join 페이지네이션 쿼리로 {items,page,hasNext}를 반환한다")
    void mySchedules_paginated() throws Exception {
        User me = saveUser("me@galpi.com", "나");
        Quote quote = saveQuote(me.getId(), saveWork(), Visibility.PRIVATE);
        scheduleRepository.save(QuoteSchedule.builder().userId(me.getId()).quote(quote)
                .sendTime(LocalTime.of(8, 0)).repeatType(RepeatType.DAILY).isActive(true).build());
        String token = jwtProvider.createAccessToken(me.getId());

        mockMvc.perform(get("/api/schedules/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quote.content").value("새는 알에서..."))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    @DisplayName("사용자 검색은 페이지네이션 쿼리로 {items,page,hasNext}를 반환한다")
    void userSearch_paginated() throws Exception {
        User me = saveUser("searcher@galpi.com", "검색자");
        saveUser("target@galpi.com", "책벌레");
        String token = jwtProvider.createAccessToken(me.getId());

        mockMvc.perform(get("/api/users/search?query=책벌레").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].nickname").value("책벌레"))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    @DisplayName("피드는 fetch-join 페이지네이션 쿼리로 팔로우한 사람의 FOLLOWERS 대사를 반환한다")
    void feed_paginated() throws Exception {
        User me = saveUser("reader@galpi.com", "독자");
        User author = saveUser("author@galpi.com", "작가");
        followRepository.save(Follow.builder().followerId(me.getId()).followingId(author.getId()).build());
        saveQuote(author.getId(), saveWork(), Visibility.FOLLOWERS);
        String token = jwtProvider.createAccessToken(me.getId());

        mockMvc.perform(get("/api/feed").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].author.nickname").value("작가"))
                .andExpect(jsonPath("$.items[0].work.title").value("데미안"))
                .andExpect(jsonPath("$.page").value(0));
    }

    @Test
    @DisplayName("책 상세=대사 모아보기는 출처와 페이지네이션된 대사 목록을 반환한다")
    void workQuotes_paginated() throws Exception {
        User me = saveUser("collector@galpi.com", "수집가");
        Work work = saveWork();
        saveQuote(me.getId(), work, Visibility.PRIVATE);
        String token = jwtProvider.createAccessToken(me.getId());

        mockMvc.perform(get("/api/works/" + work.getId() + "/quotes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.work.title").value("데미안"))
                .andExpect(jsonPath("$.quotes.items[0].content").value("새는 알에서..."))
                .andExpect(jsonPath("$.quotes.page").value(0));
    }
}
