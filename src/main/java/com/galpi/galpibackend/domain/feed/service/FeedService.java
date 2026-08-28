package com.galpi.galpibackend.domain.feed.service;

import com.galpi.galpibackend.domain.feed.dto.FeedResponse;
import com.galpi.galpibackend.domain.feed.dto.FeedResponse.Author;
import com.galpi.galpibackend.domain.feed.dto.FeedResponse.FeedItem;
import com.galpi.galpibackend.domain.feed.dto.FeedResponse.WorkRef;
import com.galpi.galpibackend.domain.follow.repository.FollowRepository;
import com.galpi.galpibackend.domain.like.repository.LikeRepository;
import com.galpi.galpibackend.domain.like.repository.LikeRepository.QuoteLikeCount;
import com.galpi.galpibackend.domain.quote.entity.Quote;
import com.galpi.galpibackend.domain.quote.entity.Visibility;
import com.galpi.galpibackend.domain.quote.repository.QuoteRepository;
import com.galpi.galpibackend.domain.user.entity.User;
import com.galpi.galpibackend.domain.user.repository.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FeedService {

    private final FollowRepository followRepository;
    private final QuoteRepository quoteRepository;
    private final LikeRepository likeRepository;
    private final UserRepository userRepository;

    public FeedService(FollowRepository followRepository, QuoteRepository quoteRepository,
                       LikeRepository likeRepository, UserRepository userRepository) {
        this.followRepository = followRepository;
        this.quoteRepository = quoteRepository;
        this.likeRepository = likeRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public FeedResponse getFeed(Long userId, int page, int size) {
        List<Long> followingIds = followRepository.findFollowingIds(userId);
        if (followingIds.isEmpty()) {
            return new FeedResponse(List.of(), page, false);
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Quote> quotePage = quoteRepository.findFeedQuotesWithWork(
                followingIds, Visibility.FOLLOWERS, pageable);
        List<Quote> quotes = quotePage.getContent();
        if (quotes.isEmpty()) {
            return new FeedResponse(List.of(), page, quotePage.hasNext());
        }

        List<Long> quoteIds = quotes.stream().map(Quote::getId).toList();
        Map<Long, String> nicknames = loadNicknames(quotes);
        Map<Long, Long> likeCounts = loadLikeCounts(quoteIds);
        Set<Long> likedQuoteIds = Set.copyOf(likeRepository.findLikedQuoteIdsIn(userId, quoteIds));

        List<FeedItem> items = quotes.stream()
                .map(quote -> new FeedItem(
                        quote.getId(),
                        quote.getContent(),
                        quote.getCharacterName(),
                        new Author(quote.getUserId(), nicknames.getOrDefault(quote.getUserId(), "")),
                        new WorkRef(quote.getWork().getTitle(), quote.getWork().getAuthor()),
                        likeCounts.getOrDefault(quote.getId(), 0L),
                        likedQuoteIds.contains(quote.getId())))
                .toList();

        return new FeedResponse(items, page, quotePage.hasNext());
    }

    private Map<Long, String> loadNicknames(List<Quote> quotes) {
        List<Long> authorIds = quotes.stream().map(Quote::getUserId).distinct().toList();
        return userRepository.findAllById(authorIds).stream()
                .collect(Collectors.toMap(User::getId, User::getNickname));
    }

    private Map<Long, Long> loadLikeCounts(List<Long> quoteIds) {
        return likeRepository.countByQuoteIdIn(quoteIds).stream()
                .collect(Collectors.toMap(QuoteLikeCount::getQuoteId, QuoteLikeCount::getCount));
    }
}
