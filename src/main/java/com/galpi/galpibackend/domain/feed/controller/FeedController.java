package com.galpi.galpibackend.domain.feed.controller;

import com.galpi.galpibackend.domain.feed.dto.FeedResponse;
import com.galpi.galpibackend.domain.feed.service.FeedService;
import com.galpi.galpibackend.global.security.CurrentUserId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/feed")
public class FeedController {

    private final FeedService feedService;

    public FeedController(FeedService feedService) {
        this.feedService = feedService;
    }

    @GetMapping
    public ResponseEntity<FeedResponse> getFeed(
            @CurrentUserId Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(feedService.getFeed(userId, page, size));
    }
}
