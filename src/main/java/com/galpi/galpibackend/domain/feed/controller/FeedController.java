package com.galpi.galpibackend.domain.feed.controller;

import com.galpi.galpibackend.domain.feed.dto.FeedResponse;
import com.galpi.galpibackend.domain.feed.service.FeedService;
import com.galpi.galpibackend.global.security.CurrentUserId;
import com.galpi.galpibackend.global.web.ApiPaging;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/feed")
@Validated
public class FeedController {

    private final FeedService feedService;

    public FeedController(FeedService feedService) {
        this.feedService = feedService;
    }

    @GetMapping
    public ResponseEntity<FeedResponse> getFeed(
            @CurrentUserId Long userId,
            @RequestParam(defaultValue = ApiPaging.DEFAULT_PAGE) @Min(0) int page,
            @RequestParam(defaultValue = ApiPaging.DEFAULT_SIZE) @Min(1) int size) {
        return ResponseEntity.ok(feedService.getFeed(userId, page, size));
    }
}
