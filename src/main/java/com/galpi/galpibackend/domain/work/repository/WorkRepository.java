package com.galpi.galpibackend.domain.work.repository;

import com.galpi.galpibackend.domain.work.entity.Work;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WorkRepository extends JpaRepository<Work, Long> {

    Optional<Work> findByIsbn(String isbn);

    /**
     * MANUAL 책 재사용 판정. author가 null일 때도 정상 매칭되도록 null-safe 하게 비교한다.
     * (파생 쿼리는 author=null을 'author = NULL'로 만들어 항상 거짓이 되므로 사용 불가)
     */
    @Query("select w from Work w "
            + "where w.ownerUserId = :ownerUserId and w.title = :title "
            + "and ((:author is null and w.author is null) or w.author = :author)")
    Optional<Work> findManualWork(@Param("ownerUserId") Long ownerUserId,
                                  @Param("title") String title,
                                  @Param("author") String author);
}
