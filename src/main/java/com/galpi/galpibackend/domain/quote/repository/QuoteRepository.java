package com.galpi.galpibackend.domain.quote.repository;

import com.galpi.galpibackend.domain.quote.entity.Quote;
import com.galpi.galpibackend.domain.quote.entity.Visibility;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuoteRepository extends JpaRepository<Quote, Long> {

    Page<Quote> findByUserIdAndWorkIdOrderByCreatedAtDesc(Long userId, Long workId, Pageable pageable);

    // 소프트 삭제된 대사는 @SQLRestriction으로 제외되어 카운트에서도 빠진다.
    long countByUserId(Long userId);

    // 출처(work)를 fetch join으로 함께 로딩해 N+1을 방지한다. (프로필 공개 대사, 페이지네이션)
    @Query(value = "select q from Quote q join fetch q.work "
            + "where q.userId = :userId and q.visibility = :visibility "
            + "order by q.createdAt desc",
            countQuery = "select count(q) from Quote q "
            + "where q.userId = :userId and q.visibility = :visibility")
    Page<Quote> findVisibleQuotesWithWork(@Param("userId") Long userId,
                                          @Param("visibility") Visibility visibility,
                                          Pageable pageable);

    // 피드: 출처를 fetch join. work는 ManyToOne이라 페이지네이션과 함께 써도 안전하다.
    @Query(value = "select q from Quote q join fetch q.work "
            + "where q.userId in :userIds and q.visibility = :visibility "
            + "order by q.createdAt desc",
            countQuery = "select count(q) from Quote q "
            + "where q.userId in :userIds and q.visibility = :visibility")
    Page<Quote> findFeedQuotesWithWork(@Param("userIds") List<Long> userIds,
                                       @Param("visibility") Visibility visibility,
                                       Pageable pageable);

    /**
     * 여러 책에 대한 사용자의 대사 개수를 한 번에 집계한다. (책장 조회 N+1 방지)
     */
    @Query("select q.work.id as workId, count(q) as count "
            + "from Quote q "
            + "where q.userId = :userId and q.work.id in :workIds "
            + "group by q.work.id")
    List<WorkQuoteCount> countByUserIdAndWorkIdIn(@Param("userId") Long userId,
                                                  @Param("workIds") List<Long> workIds);

    interface WorkQuoteCount {
        Long getWorkId();

        long getCount();
    }
}
