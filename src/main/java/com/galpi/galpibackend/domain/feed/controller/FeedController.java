package com.galpi.galpibackend.domain.feed.controller;

import com.galpi.galpibackend.domain.feed.dto.FeedItem;
import com.galpi.galpibackend.domain.feed.service.FeedService;
import com.galpi.galpibackend.global.security.CurrentUserId;
import com.galpi.galpibackend.global.web.ApiPaging;
import com.galpi.galpibackend.global.web.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "피드", description = "팔로우한 사람들이 FOLLOWERS로 공개한 대사를 최신순으로 모아봅니다. 각 대사에 출처가 포함됩니다.")
@RestController
@RequestMapping("/api/feed")
@Validated
public class FeedController {

    private final FeedService feedService;

    public FeedController(FeedService feedService) {
        this.feedService = feedService;
    }

    @Operation(summary = "팔로잉 피드", description = "팔로우한 사용자들의 FOLLOWERS 대사를 페이지네이션해 반환합니다(작성자·출처·좋아요 정보 포함).")
    @GetMapping
    public ResponseEntity<PageResponse<FeedItem>> getFeed(
            @CurrentUserId Long userId,
            @Parameter(description = "페이지 번호(0부터)") @RequestParam(defaultValue = ApiPaging.DEFAULT_PAGE) @Min(0) int page,
            @Parameter(description = "페이지당 개수") @RequestParam(defaultValue = ApiPaging.DEFAULT_SIZE) @Min(1) int size) {
        return ResponseEntity.ok(feedService.getFeed(userId, page, size));
    }
}
