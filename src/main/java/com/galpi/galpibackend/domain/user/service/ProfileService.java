package com.galpi.galpibackend.domain.user.service;

import com.galpi.galpibackend.domain.follow.repository.FollowRepository;
import com.galpi.galpibackend.domain.quote.entity.Quote;
import com.galpi.galpibackend.domain.quote.entity.Visibility;
import com.galpi.galpibackend.domain.quote.repository.QuoteRepository;
import com.galpi.galpibackend.domain.user.dto.ProfileQuote;
import com.galpi.galpibackend.domain.user.dto.ProfileResponse;
import com.galpi.galpibackend.domain.user.entity.User;
import com.galpi.galpibackend.domain.user.repository.UserRepository;
import com.galpi.galpibackend.domain.work.dto.WorkSource;
import com.galpi.galpibackend.global.error.CustomException;
import com.galpi.galpibackend.global.error.ErrorCode;
import com.galpi.galpibackend.global.web.PageResponse;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final QuoteRepository quoteRepository;

    public ProfileService(UserRepository userRepository, FollowRepository followRepository,
                          QuoteRepository quoteRepository) {
        this.userRepository = userRepository;
        this.followRepository = followRepository;
        this.quoteRepository = quoteRepository;
    }

    @Transactional(readOnly = true)
    public ProfileResponse getProfile(Long requesterId, Long targetId, int page, int size) {
        User target = userRepository.findById(targetId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        boolean isSelf = requesterId.equals(targetId);
        boolean isFollowing = !isSelf
                && followRepository.existsByFollowerIdAndFollowingId(requesterId, targetId);

        long followerCount = followRepository.countByFollowingId(targetId);
        long followingCount = followRepository.countByFollowerId(targetId);

        // 본인이거나 팔로우 중일 때만 FOLLOWERS 공개 대사를 노출
        PageResponse<ProfileQuote> quotes;
        if (isSelf || isFollowing) {
            Pageable pageable = PageRequest.of(page, size);
            Page<Quote> quotePage = quoteRepository
                    .findVisibleQuotesWithWork(targetId, Visibility.FOLLOWERS, pageable);
            List<ProfileQuote> items = quotePage.getContent().stream()
                    .map(this::toProfileQuote)
                    .toList();
            quotes = PageResponse.from(quotePage, items);
        } else {
            quotes = PageResponse.of(List.of(), page, false);
        }

        return new ProfileResponse(
                target.getId(),
                target.getNickname(),
                target.getBio(),
                followerCount,
                followingCount,
                isFollowing,
                quotes
        );
    }

    private ProfileQuote toProfileQuote(Quote quote) {
        return new ProfileQuote(
                quote.getId(),
                quote.getContent(),
                quote.getCharacterName(),
                WorkSource.from(quote.getWork())
        );
    }
}
