package com.galpi.galpibackend.domain.like.controller;

import com.galpi.galpibackend.domain.like.dto.LikeResponse;
import com.galpi.galpibackend.domain.like.service.LikeService;
import com.galpi.galpibackend.global.security.CurrentUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "좋아요", description = "피드 등에서 본 대사에 좋아요/취소. 한 대사에 한 번만 누를 수 있습니다.")
@RestController
@RequestMapping("/api/quotes/{quoteId}/like")
public class LikeController {

    private final LikeService likeService;

    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }

    @Operation(summary = "좋아요", description = "대사에 좋아요를 누릅니다. 이미 좋아요한 상태면 409 ALREADY_LIKED를 반환합니다. "
            + "볼 수 없는 대사(남의 PRIVATE 등)는 403.")
    @PostMapping
    public ResponseEntity<LikeResponse> like(@CurrentUserId Long userId,
                                             @Parameter(description = "대사 ID") @PathVariable Long quoteId) {
        return ResponseEntity.ok(likeService.like(userId, quoteId));
    }

    @Operation(summary = "좋아요 취소", description = "좋아요를 취소합니다. (멱등 — 좋아요하지 않은 상태여도 성공)")
    @DeleteMapping
    public ResponseEntity<LikeResponse> unlike(@CurrentUserId Long userId,
                                               @Parameter(description = "대사 ID") @PathVariable Long quoteId) {
        return ResponseEntity.ok(likeService.unlike(userId, quoteId));
    }
}
