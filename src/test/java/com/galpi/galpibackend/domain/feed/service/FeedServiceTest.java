package com.galpi.galpibackend.domain.feed.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.galpi.galpibackend.domain.feed.dto.FeedItem;
import com.galpi.galpibackend.global.web.PageResponse;
import com.galpi.galpibackend.domain.follow.repository.FollowRepository;
import com.galpi.galpibackend.domain.like.repository.LikeRepository;
import com.galpi.galpibackend.domain.like.repository.LikeRepository.QuoteLikeCount;
import com.galpi.galpibackend.domain.quote.entity.Quote;
import com.galpi.galpibackend.domain.quote.entity.Visibility;
import com.galpi.galpibackend.domain.quote.repository.QuoteRepository;
import com.galpi.galpibackend.domain.user.entity.User;
import com.galpi.galpibackend.domain.user.repository.UserRepository;
import com.galpi.galpibackend.domain.work.entity.BookSource;
import com.galpi.galpibackend.domain.work.entity.Work;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class FeedServiceTest {

    @Mock
    private FollowRepository followRepository;

    @Mock
    private QuoteRepository quoteRepository;

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FeedService feedService;

    private Quote quoteBy(long quoteId, long authorId) {
        Work work = Work.builder().source(BookSource.API)
                .title("데미안").author("헤르만 헤세").build();
        ReflectionTestUtils.setField(work, "id", 10L);
        Quote quote = Quote.builder().userId(authorId).work(work)
                .content("새는 알에서...").characterName("데미안").visibility(Visibility.FOLLOWERS).build();
        ReflectionTestUtils.setField(quote, "id", quoteId);
        return quote;
    }

    private User userWithId(long id, String nickname) {
        User user = User.builder().email(id + "@b.com").password("pw").nickname(nickname).build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    @Test
    @DisplayName("팔로우가 없으면 빈 피드를 반환한다")
    void getFeed_noFollowing() {
        given(followRepository.findFollowingIds(1L)).willReturn(List.of());

        PageResponse<FeedItem> response = feedService.getFeed(1L, 0, 20);

        assertThat(response.items()).isEmpty();
        assertThat(response.hasNext()).isFalse();
        verify(quoteRepository, never())
                .findFeedQuotesWithWork(any(), any(), any());
    }

    @Test
    @DisplayName("팔로우한 사람들의 FOLLOWERS 대사를 출처·작성자·좋아요 정보와 함께 반환한다")
    void getFeed_withQuotes() {
        given(followRepository.findFollowingIds(1L)).willReturn(List.of(2L));
        given(quoteRepository.findFeedQuotesWithWork(
                eq(List.of(2L)), eq(Visibility.FOLLOWERS), any()))
                .willReturn(new PageImpl<>(List.of(quoteBy(100L, 2L)), PageRequest.of(0, 20), 1));
        given(userRepository.findAllById(List.of(2L))).willReturn(List.of(userWithId(2L, "책친구")));
        QuoteLikeCount likeCount = new QuoteLikeCount() {
            @Override
            public Long getQuoteId() {
                return 100L;
            }

            @Override
            public long getCount() {
                return 3L;
            }
        };
        given(likeRepository.countByQuoteIdIn(List.of(100L))).willReturn(List.of(likeCount));
        given(likeRepository.findLikedQuoteIdsIn(1L, List.of(100L))).willReturn(List.of(100L));

        PageResponse<FeedItem> response = feedService.getFeed(1L, 0, 20);

        assertThat(response.items()).hasSize(1);
        FeedItem item = response.items().get(0);
        assertThat(item.quoteId()).isEqualTo(100L);
        assertThat(item.author().nickname()).isEqualTo("책친구");
        assertThat(item.work().title()).isEqualTo("데미안");
        assertThat(item.work().author()).isEqualTo("헤르만 헤세");
        assertThat(item.likeCount()).isEqualTo(3L);
        assertThat(item.isLiked()).isTrue();
    }
}
