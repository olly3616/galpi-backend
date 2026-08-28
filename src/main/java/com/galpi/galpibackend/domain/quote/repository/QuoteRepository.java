package com.galpi.galpibackend.domain.quote.repository;

import com.galpi.galpibackend.domain.quote.entity.Quote;
import com.galpi.galpibackend.domain.quote.entity.Visibility;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuoteRepository extends JpaRepository<Quote, Long> {

    List<Quote> findByUserIdAndWorkIdOrderByCreatedAtDesc(Long userId, Long workId);

    List<Quote> findByUserIdAndVisibilityOrderByCreatedAtDesc(Long userId, Visibility visibility);

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
