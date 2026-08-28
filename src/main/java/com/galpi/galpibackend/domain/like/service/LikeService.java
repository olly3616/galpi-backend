package com.galpi.galpibackend.domain.like.service;

import com.galpi.galpibackend.domain.like.dto.LikeResponse;
import com.galpi.galpibackend.domain.like.entity.Like;
import com.galpi.galpibackend.domain.like.repository.LikeRepository;
import com.galpi.galpibackend.domain.quote.repository.QuoteRepository;
import com.galpi.galpibackend.global.error.CustomException;
import com.galpi.galpibackend.global.error.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LikeService {

    private final LikeRepository likeRepository;
    private final QuoteRepository quoteRepository;

    public LikeService(LikeRepository likeRepository, QuoteRepository quoteRepository) {
        this.likeRepository = likeRepository;
        this.quoteRepository = quoteRepository;
    }

    @Transactional
    public LikeResponse like(Long userId, Long quoteId) {
        if (!quoteRepository.existsById(quoteId)) {
            throw new CustomException(ErrorCode.NOT_FOUND);
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
}
