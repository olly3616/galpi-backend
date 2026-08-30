package com.galpi.galpibackend.domain.follow.repository;

import com.galpi.galpibackend.domain.follow.entity.Follow;
import com.galpi.galpibackend.domain.user.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    /**
     * targetId를 팔로우하는 사용자들(팔로워 목록). 최근 팔로우한 순.
     */
    @Query(value = "select u from Follow f, User u "
            + "where f.followingId = :targetId and u.id = f.followerId "
            + "order by f.id desc",
            countQuery = "select count(f) from Follow f where f.followingId = :targetId")
    Page<User> findFollowers(@Param("targetId") Long targetId, Pageable pageable);

    /**
     * targetId가 팔로우하는 사용자들(팔로잉 목록). 최근 팔로우한 순.
     */
    @Query(value = "select u from Follow f, User u "
            + "where f.followerId = :targetId and u.id = f.followingId "
            + "order by f.id desc",
            countQuery = "select count(f) from Follow f where f.followerId = :targetId")
    Page<User> findFollowing(@Param("targetId") Long targetId, Pageable pageable);
}
