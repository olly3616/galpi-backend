package com.galpi.galpibackend.domain.follow.repository;

import com.galpi.galpibackend.domain.follow.entity.Follow;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);

    Optional<Follow> findByFollowerIdAndFollowingId(Long followerId, Long followingId);

    long countByFollowingId(Long followingId); // 팔로워 수

    long countByFollowerId(Long followerId);   // 팔로잉 수

    /**
     * followerId가 팔로우 중인 대상 중, 주어진 후보 목록에 포함된 ID들을 반환한다. (검색 결과 isFollowing 계산)
     */
    @Query("select f.followingId from Follow f "
            + "where f.followerId = :followerId and f.followingId in :candidateIds")
    List<Long> findFollowingIdsIn(@Param("followerId") Long followerId,
                                  @Param("candidateIds") List<Long> candidateIds);

    /**
     * followerId가 팔로우 중인 모든 대상 ID. (피드 조회용)
     */
    @Query("select f.followingId from Follow f where f.followerId = :followerId")
    List<Long> findFollowingIds(@Param("followerId") Long followerId);
}
