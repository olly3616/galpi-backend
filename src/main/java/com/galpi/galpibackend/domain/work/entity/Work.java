package com.galpi.galpibackend.domain.work.entity;

import com.galpi.galpibackend.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
// ISBN에 유니크 제약: 동시 추가 시에도 같은 ISBN의 Work가 중복 생성되지 않도록 DB 레벨에서 보장.
// (MANUAL 책은 isbn=null이며, NULL은 유니크 제약에서 서로 구별되므로 여러 건 허용됨)
@Table(name = "works", uniqueConstraints = @UniqueConstraint(name = "uk_work_isbn", columnNames = "isbn"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Work extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookSource source;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(length = 255)
    private String author;

    @Column(length = 255)
    private String publisher;

    @Column(length = 1024)
    private String coverUrl;

    @Column(length = 20)
    private String isbn;

    // API 책은 공용(null), MANUAL 책은 등록한 사용자 소유
    @Column
    private Long ownerUserId;

    @Builder
    private Work(BookSource source, String title, String author,
                 String publisher, String coverUrl, String isbn, Long ownerUserId) {
        this.source = source;
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.coverUrl = coverUrl;
        this.isbn = isbn;
        this.ownerUserId = ownerUserId;
    }
}
