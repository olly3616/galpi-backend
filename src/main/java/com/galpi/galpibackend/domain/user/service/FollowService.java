package com.galpi.galpibackend.domain.user.service;

import com.galpi.galpibackend.domain.follow.entity.Follow;
import com.galpi.galpibackend.domain.follow.repository.FollowRepository;
import com.galpi.galpibackend.domain.user.dto.FollowResponse;
import com.galpi.galpibackend.domain.user.dto.UserSearchResponse;
import com.galpi.galpibackend.domain.user.dto.UserSearchResponse.UserSearchItem;
import com.galpi.galpibackend.domain.user.entity.User;
import com.galpi.galpibackend.domain.user.repository.UserRepository;
import com.galpi.galpibackend.global.error.CustomException;
import com.galpi.galpibackend.global.error.ErrorCode;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    public FollowService(FollowRepository followRepository, UserRepository userRepository) {
        this.followRepository = followRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserSearchResponse searchUsers(Long userId, String query) {
        List<User> users = userRepository.searchByNickname(escapeLike(query), userId);

        List<Long> userIds = users.stream().map(User::getId).toList();
        Set<Long> followingIds = userIds.isEmpty()
                ? Set.of()
                : Set.copyOf(followRepository.findFollowingIdsIn(userId, userIds));

        List<UserSearchItem> items = users.stream()
                .map(user -> new UserSearchItem(
                        user.getId(),
                        user.getNickname(),
                        user.getBio(),
                        followingIds.contains(user.getId())))
                .toList();

        return new UserSearchResponse(items);
    }

    /**
     * LIKE 패턴의 특수문자(\, %, _)를 이스케이프해, 사용자가 와일드카드로 검색 결과를 조작하지 못하게 한다.
     * 이스케이프 문자는 '\'이며 UserRepository의 escape 절과 일치한다.
     */
    private String escapeLike(String keyword) {
        if (keyword == null) {
            return "";
        }
        return keyword
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }

    @Transactional
    public FollowResponse follow(Long userId, Long targetId) {
        if (userId.equals(targetId)) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR, "자기 자신은 팔로우할 수 없습니다.");
        }
        if (!userRepository.existsById(targetId)) {
            throw new CustomException(ErrorCode.NOT_FOUND);
        }
        if (!followRepository.existsByFollowerIdAndFollowingId(userId, targetId)) {
            followRepository.save(Follow.builder()
                    .followerId(userId)
                    .followingId(targetId)
                    .build());
        }
        return new FollowResponse(true);
    }

    @Transactional
    public FollowResponse unfollow(Long userId, Long targetId) {
        followRepository.findByFollowerIdAndFollowingId(userId, targetId)
                .ifPresent(followRepository::delete);
        return new FollowResponse(false);
    }
}
