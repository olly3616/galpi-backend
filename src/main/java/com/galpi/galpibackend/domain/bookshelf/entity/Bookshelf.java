package com.galpi.galpibackend.domain.bookshelf.entity;

import com.galpi.galpibackend.domain.work.entity.Work;
import com.galpi.galpibackend.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "bookshelf",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_bookshelf_user_work",
                columnNames = {"user_id", "work_id"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Bookshelf extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "work_id", nullable = false)
    private Work work;

    @Builder
    private Bookshelf(Long userId, Work work) {
        this.userId = userId;
        this.work = work;
    }
}
