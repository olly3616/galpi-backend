package com.galpi.galpibackend.domain.like.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.galpi.galpibackend.domain.follow.repository.FollowRepository;
import com.galpi.galpibackend.domain.like.dto.LikeResponse;
import com.galpi.galpibackend.domain.like.entity.Like;
import com.galpi.galpibackend.domain.like.repository.LikeRepository;
import com.galpi.galpibackend.domain.quote.entity.Quote;
import com.galpi.galpibackend.domain.quote.entity.Visibility;
import com.galpi.galpibackend.domain.quote.repository.QuoteRepository;
import com.galpi.galpibackend.domain.work.entity.BookSource;
import com.galpi.galpibackend.domain.work.entity.BookType;
import com.galpi.galpibackend.domain.work.entity.Work;
import com.galpi.galpibackend.global.error.CustomException;
import com.galpi.galpibackend.global.error.ErrorCode;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class LikeServiceTest {

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private QuoteRepository quoteRepository;

    @Mock
    private FollowRepository followRepository;

    @InjectMocks
    private LikeService likeService;

    private Quote quote(long quoteId, long authorId, Visibility visibility) {
        Work work = Work.builder().source(BookSource.API).type(BookType.NOVEL)
                .title("데미안").author("헤르만 헤세").build();
        ReflectionTestUtils.setField(work, "id", 10L);
        Quote q = Quote.builder().userId(authorId).work(work)
                .content("새는 알에서 …").visibility(visibility).build();
        ReflectionTestUtils.setField(q, "id", quoteId);
        return q;
    }

    @Test
    @DisplayName("팔로우한 작성자의 FOLLOWERS 대사에 좋아요하면 저장하고 liked=true를 반환한다")
    void like_success() {
        given(quoteRepository.findById(100L)).willReturn(Optional.of(quote(100L, 2L, Visibility.FOLLOWERS)));
        given(followRepository.existsByFollowerIdAndFollowingId(1L, 2L)).willReturn(true);
        given(likeRepository.existsByUserIdAndQuoteId(1L, 100L)).willReturn(false);
        given(likeRepository.countByQuoteId(100L)).willReturn(4L);

        LikeResponse response = likeService.like(1L, 100L);

        assertThat(response.liked()).isTrue();
        assertThat(response.likeCount()).isEqualTo(4L);
        verify(likeRepository).save(any(Like.class));
    }

    @Test
    @DisplayName("본인 대사에는 팔로우 없이도 좋아요할 수 있다")
    void like_ownQuote() {
        given(quoteRepository.findById(100L)).willReturn(Optional.of(quote(100L, 1L, Visibility.PRIVATE)));
        given(likeRepository.existsByUserIdAndQuoteId(1L, 100L)).willReturn(false);
        given(likeRepository.countByQuoteId(100L)).willReturn(1L);

        LikeResponse response = likeService.like(1L, 100L);

        assertThat(response.liked()).isTrue();
        verify(likeRepository).save(any(Like.class));
    }

    @Test
    @DisplayName("볼 수 없는 대사(남의 PRIVATE)에 좋아요하면 FORBIDDEN 예외를 던진다")
    void like_forbidden() {
        given(quoteRepository.findById(100L)).willReturn(Optional.of(quote(100L, 2L, Visibility.PRIVATE)));

        assertThatThrownBy(() -> likeService.like(1L, 100L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.FORBIDDEN);

        verify(likeRepository, never()).save(any(Like.class));
    }

    @Test
    @DisplayName("팔로우하지 않은 작성자의 FOLLOWERS 대사에 좋아요하면 FORBIDDEN 예외를 던진다")
    void like_followersButNotFollowing() {
        given(quoteRepository.findById(100L)).willReturn(Optional.of(quote(100L, 2L, Visibility.FOLLOWERS)));
        given(followRepository.existsByFollowerIdAndFollowingId(1L, 2L)).willReturn(false);

        assertThatThrownBy(() -> likeService.like(1L, 100L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.FORBIDDEN);
    }

    @Test
    @DisplayName("이미 좋아요한 대사에 다시 좋아요하면 ALREADY_LIKED 예외를 던진다")
    void like_alreadyLiked() {
        given(quoteRepository.findById(100L)).willReturn(Optional.of(quote(100L, 1L, Visibility.PRIVATE)));
        given(likeRepository.existsByUserIdAndQuoteId(1L, 100L)).willReturn(true);

        assertThatThrownBy(() -> likeService.like(1L, 100L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.ALREADY_LIKED);

        verify(likeRepository, never()).save(any(Like.class));
    }

    @Test
    @DisplayName("존재하지 않는 대사에 좋아요하면 NOT_FOUND 예외를 던진다")
    void like_quoteNotFound() {
        given(quoteRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> likeService.like(1L, 99L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.NOT_FOUND);
    }

    @Test
    @DisplayName("좋아요 취소 시 삭제하고 liked=false와 갱신된 개수를 반환한다")
    void unlike_success() {
        Like like = Like.builder().userId(1L).quoteId(100L).build();
        given(likeRepository.findByUserIdAndQuoteId(1L, 100L)).willReturn(Optional.of(like));
        given(likeRepository.countByQuoteId(100L)).willReturn(3L);

        LikeResponse response = likeService.unlike(1L, 100L);

        assertThat(response.liked()).isFalse();
        assertThat(response.likeCount()).isEqualTo(3L);
        verify(likeRepository).delete(like);
    }
}
