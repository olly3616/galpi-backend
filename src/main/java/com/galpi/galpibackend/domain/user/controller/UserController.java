package com.galpi.galpibackend.domain.user.controller;

import com.galpi.galpibackend.domain.user.dto.FollowResponse;
import com.galpi.galpibackend.domain.user.dto.ProfileResponse;
import com.galpi.galpibackend.domain.user.dto.UserSearchItem;
import com.galpi.galpibackend.domain.user.service.FollowService;
import com.galpi.galpibackend.domain.user.service.ProfileService;
import com.galpi.galpibackend.global.security.CurrentUserId;
import com.galpi.galpibackend.global.web.ApiPaging;
import com.galpi.galpibackend.global.web.PageResponse;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {

    private final FollowService followService;
    private final ProfileService profileService;

    public UserController(FollowService followService, ProfileService profileService) {
        this.followService = followService;
        this.profileService = profileService;
    }

    @GetMapping("/search")
    public ResponseEntity<PageResponse<UserSearchItem>> searchUsers(
            @CurrentUserId Long userId,
            @RequestParam String query,
            @RequestParam(defaultValue = ApiPaging.DEFAULT_PAGE) @Min(0) int page,
            @RequestParam(defaultValue = ApiPaging.DEFAULT_SIZE) @Min(1) int size) {
        return ResponseEntity.ok(followService.searchUsers(userId, query, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfileResponse> getProfile(
            @CurrentUserId Long userId,
            @PathVariable Long id,
            @RequestParam(defaultValue = ApiPaging.DEFAULT_PAGE) @Min(0) int page,
            @RequestParam(defaultValue = ApiPaging.DEFAULT_SIZE) @Min(1) int size) {
        return ResponseEntity.ok(profileService.getProfile(userId, id, page, size));
    }

    @PostMapping("/{id}/follow")
    public ResponseEntity<FollowResponse> follow(@CurrentUserId Long userId,
                                                 @PathVariable Long id) {
        return ResponseEntity.ok(followService.follow(userId, id));
    }

    @DeleteMapping("/{id}/follow")
    public ResponseEntity<FollowResponse> unfollow(@CurrentUserId Long userId,
                                                   @PathVariable Long id) {
        return ResponseEntity.ok(followService.unfollow(userId, id));
    }
}
