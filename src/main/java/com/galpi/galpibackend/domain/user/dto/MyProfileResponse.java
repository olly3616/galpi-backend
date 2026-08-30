package com.galpi.galpibackend.domain.user.dto;

/**
 * 내 프로필(GET/PATCH /api/users/me) 응답. 팔로워/팔로잉/책/문장 카운트를 포함한다.
 */
public record MyProfileResponse(
        Long userId,
        String nickname,
        String bio,
        String profileImageUrl,
        long followerCount,
        long followingCount,
        long bookCount,
        long quoteCount
) {
}
