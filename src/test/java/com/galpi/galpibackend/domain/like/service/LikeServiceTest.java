package com.galpi.galpibackend.domain.like.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.galpi.galpibackend.domain.like.dto.LikeResponse;
import com.galpi.galpibackend.domain.like.entity.Like;
import com.galpi.galpibackend.domain.like.repository.LikeRepository;
import com.galpi.galpibackend.domain.quote.repository.QuoteRepository;
import com.galpi.galpibackend.global.error.CustomException;
import com.galpi.galpibackend.global.error.ErrorCode;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LikeServiceTest {

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private QuoteRepository quoteRepository;

    @InjectMocks
    private LikeService likeService;

    @Test
    @DisplayName("좋아요 성공 시 저장하고 liked=true와 갱신된 개수를 반환한다")
    void like_success() {
        given(quoteRepository.existsById(100L)).willReturn(true);
        given(likeRepository.existsByUserIdAndQuoteId(1L, 100L)).willReturn(false);
        given(likeRepository.countByQuoteId(100L)).willReturn(4L);

        LikeResponse response = likeService.like(1L, 100L);

        assertThat(response.liked()).isTrue();
        assertThat(response.likeCount()).isEqualTo(4L);
        verify(likeRepository).save(any(Like.class));
    }

    @Test
    @DisplayName("이미 좋아요한 상태면 중복 저장하지 않는다")
    void like_alreadyLiked() {
        given(quoteRepository.existsById(100L)).willReturn(true);
        given(likeRepository.existsByUserIdAndQuoteId(1L, 100L)).willReturn(true);
        given(likeRepository.countByQuoteId(100L)).willReturn(4L);

        LikeResponse response = likeService.like(1L, 100L);

        assertThat(response.liked()).isTrue();
        verify(likeRepository, never()).save(any(Like.class));
    }

    @Test
    @DisplayName("존재하지 않는 대사에 좋아요하면 NOT_FOUND 예외를 던진다")
    void like_quoteNotFound() {
        given(quoteRepository.existsById(99L)).willReturn(false);

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
