package com.galpi.galpibackend.domain.user.controller;

import com.galpi.galpibackend.domain.user.dto.FollowResponse;
import com.galpi.galpibackend.domain.user.dto.MyProfileResponse;
import com.galpi.galpibackend.domain.user.dto.ProfileResponse;
import com.galpi.galpibackend.domain.user.dto.UpdateProfileRequest;
import com.galpi.galpibackend.domain.user.dto.UserSearchItem;
import com.galpi.galpibackend.domain.user.service.FollowService;
import com.galpi.galpibackend.domain.user.service.ProfileService;
import com.galpi.galpibackend.global.security.CurrentUserId;
import com.galpi.galpibackend.global.web.ApiPaging;
import com.galpi.galpibackend.global.web.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @Operation(summary = "내 프로필 조회",
            description = "로그인한 본인의 프로필과 팔로워/팔로잉/책/문장 수를 조회합니다. (아바타 표시에 profileImageUrl 사용)")
    @GetMapping("/me")
    public ResponseEntity<MyProfileResponse> getMyProfile(@CurrentUserId Long userId) {
        return ResponseEntity.ok(profileService.getMyProfile(userId));
    }

    @Operation(summary = "내 프로필 수정",
            description = "닉네임·소개·프로필 이미지 URL을 부분 수정합니다. 전달한 필드만 변경됩니다. "
                    + "profileImageUrl은 /api/images로 업로드해 받은 url을 넣으세요. 닉네임 중복 시 409.")
    @PatchMapping("/me")
    public ResponseEntity<MyProfileResponse> updateMyProfile(
            @CurrentUserId Long userId,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(profileService.updateMyProfile(userId, request));
    }

    @Operation(summary = "사용자 검색", description = "닉네임 부분 일치로 다른 사용자를 검색합니다(본인 제외). 각 결과에 팔로우 여부·아바타(profileImageUrl) 포함.")
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

    @Operation(summary = "팔로워 목록",
            description = "대상 사용자(userId)를 팔로우하는 사용자 목록을 페이지네이션해 반환합니다. 각 항목에 내가 그 사람을 팔로우 중인지(isFollowing) 포함.")
    @GetMapping("/{userId}/followers")
    public ResponseEntity<PageResponse<UserSearchItem>> getFollowers(
            @CurrentUserId Long requesterId,
            @Parameter(description = "대상 사용자 ID") @PathVariable Long userId,
            @Parameter(description = "페이지 번호(0부터)") @RequestParam(defaultValue = ApiPaging.DEFAULT_PAGE) @Min(0) int page,
            @Parameter(description = "페이지당 개수") @RequestParam(defaultValue = ApiPaging.DEFAULT_SIZE) @Min(1) int size) {
        return ResponseEntity.ok(followService.getFollowers(requesterId, userId, page, size));
    }

    @Operation(summary = "팔로잉 목록",
            description = "대상 사용자(userId)가 팔로우하는 사용자 목록을 페이지네이션해 반환합니다. 각 항목에 내가 그 사람을 팔로우 중인지(isFollowing) 포함.")
    @GetMapping("/{userId}/following")
    public ResponseEntity<PageResponse<UserSearchItem>> getFollowing(
            @CurrentUserId Long requesterId,
            @Parameter(description = "대상 사용자 ID") @PathVariable Long userId,
            @Parameter(description = "페이지 번호(0부터)") @RequestParam(defaultValue = ApiPaging.DEFAULT_PAGE) @Min(0) int page,
            @Parameter(description = "페이지당 개수") @RequestParam(defaultValue = ApiPaging.DEFAULT_SIZE) @Min(1) int size) {
        return ResponseEntity.ok(followService.getFollowing(requesterId, userId, page, size));
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
