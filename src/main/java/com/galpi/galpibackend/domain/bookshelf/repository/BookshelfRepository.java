package com.galpi.galpibackend.domain.bookshelf.repository;

import com.galpi.galpibackend.domain.bookshelf.entity.Bookshelf;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookshelfRepository extends JpaRepository<Bookshelf, Long> {

    boolean existsByUserIdAndWorkId(Long userId, Long workId);

    Optional<Bookshelf> findByUserIdAndWorkId(Long userId, Long workId);

    Page<Bookshelf> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
