package com.galpi.galpibackend.domain.like.controller;

import com.galpi.galpibackend.domain.like.dto.LikeResponse;
import com.galpi.galpibackend.domain.like.service.LikeService;
import com.galpi.galpibackend.global.security.CurrentUserId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/quotes/{id}/like")
public class LikeController {

    private final LikeService likeService;

    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }

    @PostMapping
    public ResponseEntity<LikeResponse> like(@CurrentUserId Long userId, @PathVariable Long id) {
        return ResponseEntity.ok(likeService.like(userId, id));
    }

    @DeleteMapping
    public ResponseEntity<LikeResponse> unlike(@CurrentUserId Long userId, @PathVariable Long id) {
        return ResponseEntity.ok(likeService.unlike(userId, id));
    }
}
