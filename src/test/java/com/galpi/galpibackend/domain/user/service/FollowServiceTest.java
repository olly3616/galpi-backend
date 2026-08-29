package com.galpi.galpibackend.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.galpi.galpibackend.domain.follow.entity.Follow;
import com.galpi.galpibackend.domain.follow.repository.FollowRepository;
import com.galpi.galpibackend.domain.user.dto.FollowResponse;
import com.galpi.galpibackend.domain.user.dto.UserSearchItem;
import com.galpi.galpibackend.domain.user.entity.User;
import com.galpi.galpibackend.domain.user.repository.UserRepository;
import com.galpi.galpibackend.global.error.CustomException;
import com.galpi.galpibackend.global.error.ErrorCode;
import com.galpi.galpibackend.global.web.PageResponse;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class FollowServiceTest {

    @Mock
    private FollowRepository followRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FollowService followService;

    private User userWithId(long id, String nickname) {
        User user = User.builder().email(id + "@b.com").password("pw").nickname(nickname).bio("소개").build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    @Test
    @DisplayName("사용자 검색 시 팔로우 여부(isFollowing)를 함께 표시한다")
    void searchUsers_marksFollowing() {
        given(userRepository.searchByNickname(eq("책"), eq(1L), any()))
                .willReturn(new PageImpl<>(List.of(userWithId(2L, "책벌레"), userWithId(3L, "책친구"))));
        given(followRepository.findFollowingIdsIn(1L, List.of(2L, 3L))).willReturn(List.of(2L));

        PageResponse<UserSearchItem> response = followService.searchUsers(1L, "책", 0, 20);

        assertThat(response.items()).hasSize(2);
        assertThat(response.items().get(0).userId()).isEqualTo(2L);
        assertThat(response.items().get(0).isFollowing()).isTrue();
        assertThat(response.items().get(1).userId()).isEqualTo(3L);
        assertThat(response.items().get(1).isFollowing()).isFalse();
    }

    @Test
    @DisplayName("검색어의 LIKE 와일드카드(%,_,\\)를 이스케이프해 조회한다")
    void searchUsers_escapesLikeWildcards() {
        given(userRepository.searchByNickname(eq("100\\% \\_a\\\\b"), eq(1L), any()))
                .willReturn(new PageImpl<>(List.of()));

        followService.searchUsers(1L, "100% _a\\b", 0, 20);

        verify(userRepository).searchByNickname(eq("100\\% \\_a\\\\b"), eq(1L), any());
    }

    @Test
    @DisplayName("팔로우 성공 시 관계를 저장하고 following=true를 반환한다")
    void follow_success() {
        given(userRepository.existsById(2L)).willReturn(true);
        given(followRepository.existsByFollowerIdAndFollowingId(1L, 2L)).willReturn(false);

        FollowResponse response = followService.follow(1L, 2L);

        assertThat(response.following()).isTrue();
        verify(followRepository).save(any(Follow.class));
    }

    @Test
    @DisplayName("이미 팔로우 중이면 중복 저장하지 않고 following=true를 반환한다")
    void follow_alreadyFollowing() {
        given(userRepository.existsById(2L)).willReturn(true);
        given(followRepository.existsByFollowerIdAndFollowingId(1L, 2L)).willReturn(true);

        FollowResponse response = followService.follow(1L, 2L);

        assertThat(response.following()).isTrue();
        verify(followRepository, never()).save(any(Follow.class));
    }

    @Test
    @DisplayName("자기 자신을 팔로우하면 VALIDATION_ERROR 예외를 던진다")
    void follow_self() {
        assertThatThrownBy(() -> followService.follow(1L, 1L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    @Test
    @DisplayName("존재하지 않는 사용자를 팔로우하면 NOT_FOUND 예외를 던진다")
    void follow_targetNotFound() {
        given(userRepository.existsById(99L)).willReturn(false);

        assertThatThrownBy(() -> followService.follow(1L, 99L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.NOT_FOUND);
    }

    @Test
    @DisplayName("언팔로우 시 관계가 있으면 삭제하고 following=false를 반환한다")
    void unfollow_success() {
        Follow follow = Follow.builder().followerId(1L).followingId(2L).build();
        given(followRepository.findByFollowerIdAndFollowingId(1L, 2L)).willReturn(Optional.of(follow));

        FollowResponse response = followService.unfollow(1L, 2L);

        assertThat(response.following()).isFalse();
        verify(followRepository).delete(follow);
    }
}
