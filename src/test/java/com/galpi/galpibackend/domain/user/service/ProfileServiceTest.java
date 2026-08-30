package com.galpi.galpibackend.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.galpi.galpibackend.domain.bookshelf.repository.BookshelfRepository;
import com.galpi.galpibackend.domain.follow.repository.FollowRepository;
import com.galpi.galpibackend.domain.quote.entity.Quote;
import com.galpi.galpibackend.domain.quote.entity.Visibility;
import com.galpi.galpibackend.domain.quote.repository.QuoteRepository;
import com.galpi.galpibackend.domain.user.dto.MyProfileResponse;
import com.galpi.galpibackend.domain.user.dto.ProfileResponse;
import com.galpi.galpibackend.domain.user.dto.UpdateProfileRequest;
import com.galpi.galpibackend.domain.user.entity.User;
import com.galpi.galpibackend.domain.user.repository.UserRepository;
import com.galpi.galpibackend.domain.work.entity.BookSource;
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

    @Mock
    private BookshelfRepository bookshelfRepository;

    @InjectMocks
    private ProfileService profileService;

    private User userWithId(long id) {
        User user = User.builder().email(id + "@b.com").password("pw").nickname("책친구").bio("소개").build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Quote followersQuote() {
        Work work = Work.builder().source(BookSource.API)
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

    @Test
    @DisplayName("내 프로필 조회는 팔로워/팔로잉/책/문장 카운트를 포함한다")
    void getMyProfile_returnsCounts() {
        given(userRepository.findById(1L)).willReturn(Optional.of(userWithId(1L)));
        given(followRepository.countByFollowingId(1L)).willReturn(7L);
        given(followRepository.countByFollowerId(1L)).willReturn(8L);
        given(bookshelfRepository.countByUserId(1L)).willReturn(9L);
        given(quoteRepository.countByUserId(1L)).willReturn(10L);

        MyProfileResponse response = profileService.getMyProfile(1L);

        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.followerCount()).isEqualTo(7L);
        assertThat(response.followingCount()).isEqualTo(8L);
        assertThat(response.bookCount()).isEqualTo(9L);
        assertThat(response.quoteCount()).isEqualTo(10L);
    }

    @Test
    @DisplayName("프로필 수정은 전달한 필드만 변경하고 갱신된 값을 반환한다")
    void updateMyProfile_success() {
        User user = userWithId(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userRepository.existsByNickname("새닉네임")).willReturn(false);
        given(followRepository.countByFollowingId(1L)).willReturn(2L);
        given(followRepository.countByFollowerId(1L)).willReturn(3L);
        given(bookshelfRepository.countByUserId(1L)).willReturn(4L);
        given(quoteRepository.countByUserId(1L)).willReturn(5L);

        MyProfileResponse response = profileService.updateMyProfile(1L,
                new UpdateProfileRequest("새닉네임", "새 소개", "https://img/a.jpg"));

        assertThat(response.nickname()).isEqualTo("새닉네임");
        assertThat(response.bio()).isEqualTo("새 소개");
        assertThat(response.profileImageUrl()).isEqualTo("https://img/a.jpg");
        assertThat(user.getNickname()).isEqualTo("새닉네임");
    }

    @Test
    @DisplayName("다른 사람이 쓰는 닉네임으로 수정하면 NICKNAME_DUPLICATED 예외를 던진다")
    void updateMyProfile_nicknameDuplicated() {
        given(userRepository.findById(1L)).willReturn(Optional.of(userWithId(1L)));
        given(userRepository.existsByNickname("중복닉")).willReturn(true);

        assertThatThrownBy(() -> profileService.updateMyProfile(1L,
                new UpdateProfileRequest("중복닉", null, null)))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.NICKNAME_DUPLICATED);
    }

    @Test
    @DisplayName("현재와 같은 닉네임으로 수정하면 중복 검사를 하지 않는다")
    void updateMyProfile_sameNicknameSkipsDupCheck() {
        User user = userWithId(1L); // 닉네임 "책친구"
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(followRepository.countByFollowingId(1L)).willReturn(0L);
        given(followRepository.countByFollowerId(1L)).willReturn(0L);
        given(bookshelfRepository.countByUserId(1L)).willReturn(0L);
        given(quoteRepository.countByUserId(1L)).willReturn(0L);

        MyProfileResponse response = profileService.updateMyProfile(1L,
                new UpdateProfileRequest("책친구", null, null));

        assertThat(response.nickname()).isEqualTo("책친구");
        verify(userRepository, never()).existsByNickname(any());
    }
}
