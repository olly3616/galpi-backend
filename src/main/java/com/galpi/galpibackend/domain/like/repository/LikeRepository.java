package com.galpi.galpibackend.domain.like.repository;

import com.galpi.galpibackend.domain.like.entity.Like;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LikeRepository extends JpaRepository<Like, Long> {

    boolean existsByUserIdAndQuoteId(Long userId, Long quoteId);

    Optional<Like> findByUserIdAndQuoteId(Long userId, Long quoteId);

    long countByQuoteId(Long quoteId);

    /**
     * 여러 대사의 좋아요 개수를 한 번에 집계한다. (피드 N+1 방지)
     */
    @Query("select l.quoteId as quoteId, count(l) as count "
            + "from Like l where l.quoteId in :quoteIds group by l.quoteId")
    List<QuoteLikeCount> countByQuoteIdIn(@Param("quoteIds") List<Long> quoteIds);

    /**
     * 주어진 대사 중 사용자가 좋아요한 대사 ID들을 반환한다. (피드 isLiked 계산)
     */
    @Query("select l.quoteId from Like l where l.userId = :userId and l.quoteId in :quoteIds")
    List<Long> findLikedQuoteIdsIn(@Param("userId") Long userId,
                                   @Param("quoteIds") List<Long> quoteIds);

    interface QuoteLikeCount {
        Long getQuoteId();

        long getCount();
    }
}
