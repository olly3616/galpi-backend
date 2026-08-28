package com.galpi.galpibackend.domain.schedule.repository;

import com.galpi.galpibackend.domain.schedule.entity.QuoteSchedule;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuoteScheduleRepository extends JpaRepository<QuoteSchedule, Long> {

    // 대사·출처를 fetch join으로 함께 로딩 (내 알림 목록 N+1 방지)
    @Query("select s from QuoteSchedule s join fetch s.quote q join fetch q.work "
            + "where s.userId = :userId order by s.createdAt desc")
    List<QuoteSchedule> findByUserIdWithQuote(@Param("userId") Long userId);

    List<QuoteSchedule> findByQuoteIdOrderByCreatedAtAsc(Long quoteId);

    // 발송 배치: 대사·출처를 함께 로딩 (발송 시 N+1 방지)
    @Query("select s from QuoteSchedule s join fetch s.quote q join fetch q.work "
            + "where s.isActive = true and s.sendTime = :sendTime")
    List<QuoteSchedule> findActiveBySendTimeWithQuote(@Param("sendTime") LocalTime sendTime);

    void deleteByQuoteId(Long quoteId);

    /**
     * 주어진 대사 중 알림이 하나라도 설정된 대사 ID들을 반환한다. (모아보기 hasSchedule 계산)
     */
    @Query("select distinct s.quote.id from QuoteSchedule s where s.quote.id in :quoteIds")
    List<Long> findQuoteIdsWithScheduleIn(@Param("quoteIds") List<Long> quoteIds);
}
