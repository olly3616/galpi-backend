package com.galpi.galpibackend.domain.work.repository;

import com.galpi.galpibackend.domain.work.entity.Work;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkRepository extends JpaRepository<Work, Long> {

    Optional<Work> findByIsbn(String isbn);

    Optional<Work> findByOwnerUserIdAndTitleAndAuthor(Long ownerUserId, String title, String author);
}
