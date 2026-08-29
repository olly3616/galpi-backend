package com.galpi.galpibackend.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.galpi.galpibackend.domain.follow.repository.FollowRepository;
import com.galpi.galpibackend.domain.quote.entity.Quote;
import com.galpi.galpibackend.domain.quote.entity.Visibility;
import com.galpi.galpibackend.domain.quote.repository.QuoteRepository;
import com.galpi.galpibackend.domain.user.dto.ProfileResponse;
import com.galpi.galpibackend.domain.user.entity.User;
import com.galpi.galpibackend.domain.user.repository.UserRepository;
import com.galpi.galpibackend.domain.work.entity.BookSource;
import com.galpi.galpibackend.domain.work.entity.BookType;
import com.galpi.galpibackend.domain.work.entity.Work;
import com.galpi.galpibackend.global.error.CustomException;
import com.galpi.galpibackend.global.error.ErrorCode;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FollowRepository followRepository;

    @Mock
    private QuoteRepository quoteRepository;

    @InjectMocks
    private ProfileService profileService;

    private User userWithId(long id) {
        User user = User.builder().email(id + "@b.com").password("pw").nickname("책친구").bio("소개").build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Quote followersQuote() {
        Work work = Work.builder().source(BookSource.API).type(BookType.NOVEL)
                .title("데미안").author("헤르만 헤세").build();
        ReflectionTestUtils.setField(work, "id", 10L);
        Quote quote = Quote.builder().userId(2L).work(work)
                .content("새는 알에서...").visibility(Visibility.FOLLOWERS).build();
        ReflectionTestUtils.setField(quote, "id", 100L);
        return quote;
    }

    @Test
    @DisplayName("팔로우 중인 사용자의 프로필은 FOLLOWERS 공개 대사를 출처와 함께 노출한다")
    void getProfile_followingSeesQuotes() {
        given(userRepository.findById(2L)).willReturn(Optional.of(userWithId(2L)));
        given(followRepository.existsByFollowerIdAndFollowingId(1L, 2L)).willReturn(true);
        given(followRepository.countByFollowingId(2L)).willReturn(5L);
        given(followRepository.countByFollowerId(2L)).willReturn(3L);
        given(quoteRepository.findVisibleQuotesWithWork(eq(2L), eq(Visibility.FOLLOWERS), any()))
                .willReturn(new PageImpl<>(List.of(followersQuote())));

        ProfileResponse response = profileService.getProfile(1L, 2L, 0, 20);

        assertThat(response.isFollowing()).isTrue();
        assertThat(response.followerCount()).isEqualTo(5L);
        assertThat(response.followingCount()).isEqualTo(3L);
        assertThat(response.quotes().items()).hasSize(1);
        assertThat(response.quotes().items().get(0).work().title()).isEqualTo("데미안");
        assertThat(response.quotes().items().get(0).work().author()).isEqualTo("헤르만 헤세");
    }

    @Test
    @DisplayName("팔로우하지 않은 사용자의 프로필은 대사를 노출하지 않는다")
    void getProfile_notFollowingHidesQuotes() {
        given(userRepository.findById(2L)).willReturn(Optional.of(userWithId(2L)));
        given(followRepository.existsByFollowerIdAndFollowingId(1L, 2L)).willReturn(false);
        given(followRepository.countByFollowingId(2L)).willReturn(0L);
        given(followRepository.countByFollowerId(2L)).willReturn(0L);

        ProfileResponse response = profileService.getProfile(1L, 2L, 0, 20);

        assertThat(response.isFollowing()).isFalse();
        assertThat(response.quotes().items()).isEmpty();
        verify(quoteRepository, never())
                .findVisibleQuotesWithWork(eq(2L), eq(Visibility.FOLLOWERS), any());
    }

    @Test
    @DisplayName("본인 프로필은 팔로우 없이도 FOLLOWERS 대사를 노출한다")
    void getProfile_selfSeesQuotes() {
        given(userRepository.findById(1L)).willReturn(Optional.of(userWithId(1L)));
        given(followRepository.countByFollowingId(1L)).willReturn(0L);
        given(followRepository.countByFollowerId(1L)).willReturn(0L);
        given(quoteRepository.findVisibleQuotesWithWork(eq(1L), eq(Visibility.FOLLOWERS), any()))
                .willReturn(new PageImpl<>(List.of()));

        ProfileResponse response = profileService.getProfile(1L, 1L, 0, 20);

        assertThat(response.isFollowing()).isFalse();
        verify(followRepository, never()).existsByFollowerIdAndFollowingId(1L, 1L);
    }

    @Test
    @DisplayName("존재하지 않는 사용자의 프로필을 조회하면 NOT_FOUND 예외를 던진다")
    void getProfile_notFound() {
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.getProfile(1L, 99L, 0, 20))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.NOT_FOUND);
    }
}
