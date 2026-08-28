package com.galpi.galpibackend.domain.like.service;

import com.galpi.galpibackend.domain.follow.repository.FollowRepository;
import com.galpi.galpibackend.domain.like.dto.LikeResponse;
import com.galpi.galpibackend.domain.like.entity.Like;
import com.galpi.galpibackend.domain.like.repository.LikeRepository;
import com.galpi.galpibackend.domain.quote.entity.Quote;
import com.galpi.galpibackend.domain.quote.entity.Visibility;
import com.galpi.galpibackend.domain.quote.repository.QuoteRepository;
import com.galpi.galpibackend.global.error.CustomException;
import com.galpi.galpibackend.global.error.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LikeService {

    private final LikeRepository likeRepository;
    private final QuoteRepository quoteRepository;
    private final FollowRepository followRepository;

    public LikeService(LikeRepository likeRepository, QuoteRepository quoteRepository,
                       FollowRepository followRepository) {
        this.likeRepository = likeRepository;
        this.quoteRepository = quoteRepository;
        this.followRepository = followRepository;
    }

    @Transactional
    public LikeResponse like(Long userId, Long quoteId) {
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
        // 볼 수 없는 대사(남의 PRIVATE, 팔로우 안 한 FOLLOWERS)에는 좋아요 불가
        if (!canAccess(userId, quote)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
        if (!likeRepository.existsByUserIdAndQuoteId(userId, quoteId)) {
            likeRepository.save(Like.builder().userId(userId).quoteId(quoteId).build());
        }
        return new LikeResponse(true, likeRepository.countByQuoteId(quoteId));
    }

    @Transactional
    public LikeResponse unlike(Long userId, Long quoteId) {
        likeRepository.findByUserIdAndQuoteId(userId, quoteId)
                .ifPresent(likeRepository::delete);
        return new LikeResponse(false, likeRepository.countByQuoteId(quoteId));
    }

    /**
     * 대사 읽기 권한: 본인 것이거나, (작성자를 팔로우 중 AND visibility=FOLLOWERS)일 때만 접근 가능.
     */
    private boolean canAccess(Long userId, Quote quote) {
        if (quote.isOwnedBy(userId)) {
            return true;
        }
        return quote.getVisibility() == Visibility.FOLLOWERS
                && followRepository.existsByFollowerIdAndFollowingId(userId, quote.getUserId());
    }
}
