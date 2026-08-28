package com.galpi.galpibackend.domain.schedule.repository;

import com.galpi.galpibackend.domain.schedule.entity.QuoteSchedule;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuoteScheduleRepository extends JpaRepository<QuoteSchedule, Long> {

    List<QuoteSchedule> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<QuoteSchedule> findByQuoteIdOrderByCreatedAtAsc(Long quoteId);

    List<QuoteSchedule> findByIsActiveTrueAndSendTime(LocalTime sendTime);

    void deleteByQuoteId(Long quoteId);

    /**
     * 주어진 대사 중 알림이 하나라도 설정된 대사 ID들을 반환한다. (모아보기 hasSchedule 계산)
     */
    @Query("select distinct s.quote.id from QuoteSchedule s where s.quote.id in :quoteIds")
    List<Long> findQuoteIdsWithScheduleIn(@Param("quoteIds") List<Long> quoteIds);
}
