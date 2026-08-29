package com.galpi.galpibackend.domain.user.controller;

import com.galpi.galpibackend.domain.user.dto.FollowResponse;
import com.galpi.galpibackend.domain.user.dto.ProfileResponse;
import com.galpi.galpibackend.domain.user.dto.UserSearchItem;
import com.galpi.galpibackend.domain.user.service.FollowService;
import com.galpi.galpibackend.domain.user.service.ProfileService;
import com.galpi.galpibackend.global.security.CurrentUserId;
import com.galpi.galpibackend.global.web.ApiPaging;
import com.galpi.galpibackend.global.web.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "사용자·팔로우", description = "사용자 검색, 프로필 조회, 팔로우/언팔로우. userId는 대상 사용자의 ID입니다.")
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

    @Operation(summary = "사용자 검색", description = "닉네임 부분 일치로 다른 사용자를 검색합니다(본인 제외). 각 결과에 팔로우 여부 포함.")
    @GetMapping("/search")
    public ResponseEntity<PageResponse<UserSearchItem>> searchUsers(
            @CurrentUserId Long userId,
            @Parameter(description = "닉네임 검색어") @RequestParam String query,
            @Parameter(description = "페이지 번호(0부터)") @RequestParam(defaultValue = ApiPaging.DEFAULT_PAGE) @Min(0) int page,
            @Parameter(description = "페이지당 개수") @RequestParam(defaultValue = ApiPaging.DEFAULT_SIZE) @Min(1) int size) {
        return ResponseEntity.ok(followService.searchUsers(userId, query, page, size));
    }

    @Operation(summary = "사용자 프로필 조회",
            description = "프로필과 팔로워/팔로잉 수를 조회합니다. 본인이거나 팔로우 중일 때만 FOLLOWERS 공개 대사가 노출되며(그 외 빈 목록), 대사는 페이지네이션됩니다.")
    @GetMapping("/{userId}")
    public ResponseEntity<ProfileResponse> getProfile(
            @CurrentUserId Long requesterId,
            @Parameter(description = "대상 사용자 ID") @PathVariable Long userId,
            @Parameter(description = "대사 페이지 번호(0부터)") @RequestParam(defaultValue = ApiPaging.DEFAULT_PAGE) @Min(0) int page,
            @Parameter(description = "페이지당 개수") @RequestParam(defaultValue = ApiPaging.DEFAULT_SIZE) @Min(1) int size) {
        return ResponseEntity.ok(profileService.getProfile(requesterId, userId, page, size));
    }

    @Operation(summary = "팔로우", description = "대상 사용자를 팔로우합니다. 자기 자신은 불가, 이미 팔로우 중이면 멱등 처리.")
    @PostMapping("/{userId}/follow")
    public ResponseEntity<FollowResponse> follow(
            @CurrentUserId Long requesterId,
            @Parameter(description = "대상 사용자 ID") @PathVariable Long userId) {
        return ResponseEntity.ok(followService.follow(requesterId, userId));
    }

    @Operation(summary = "언팔로우", description = "대상 사용자를 언팔로우합니다. (멱등)")
    @DeleteMapping("/{userId}/follow")
    public ResponseEntity<FollowResponse> unfollow(
            @CurrentUserId Long requesterId,
            @Parameter(description = "대상 사용자 ID") @PathVariable Long userId) {
        return ResponseEntity.ok(followService.unfollow(requesterId, userId));
    }
}
