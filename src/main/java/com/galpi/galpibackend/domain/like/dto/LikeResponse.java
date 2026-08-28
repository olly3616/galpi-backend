package com.galpi.galpibackend.domain.like.dto;

public record LikeResponse(
        boolean liked,
        long likeCount
) {
}
