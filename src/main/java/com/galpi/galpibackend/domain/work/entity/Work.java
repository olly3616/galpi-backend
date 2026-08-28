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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "works")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Work extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookSource source;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookType type;

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
    private Work(BookSource source, BookType type, String title, String author,
                 String publisher, String coverUrl, String isbn, Long ownerUserId) {
        this.source = source;
        this.type = type;
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.coverUrl = coverUrl;
        this.isbn = isbn;
        this.ownerUserId = ownerUserId;
    }
}
