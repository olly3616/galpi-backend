package com.galpi.galpibackend.domain.like.entity;

import com.galpi.galpibackend.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(
        name = "likes",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_like_user_quote",
                columnNames = {"user_id", "quote_id"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Like extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "quote_id", nullable = false)
    private Long quoteId;

    @Builder
    private Like(Long userId, Long quoteId) {
        this.userId = userId;
        this.quoteId = quoteId;
    }
}
