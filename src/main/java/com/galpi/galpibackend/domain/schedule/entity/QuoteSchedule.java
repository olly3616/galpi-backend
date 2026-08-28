package com.galpi.galpibackend.domain.schedule.entity;

import com.galpi.galpibackend.domain.quote.entity.Quote;
import com.galpi.galpibackend.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "quote_schedules")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuoteSchedule extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quote_id", nullable = false)
    private Quote quote;

    @Column(nullable = false)
    private LocalTime sendTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RepeatType repeatType;

    // WEEKLY일 때 "MON,WED,FRI" 형태
    @Column(length = 50)
    private String daysOfWeek;

    @Column(nullable = false)
    private boolean isActive;

    private LocalDateTime lastSentAt;

    @Builder
    private QuoteSchedule(Long userId, Quote quote, LocalTime sendTime, RepeatType repeatType,
                          String daysOfWeek, boolean isActive) {
        this.userId = userId;
        this.quote = quote;
        this.sendTime = sendTime;
        this.repeatType = repeatType;
        this.daysOfWeek = daysOfWeek;
        this.isActive = isActive;
    }

    public boolean isOwnedBy(Long userId) {
        return this.userId.equals(userId);
    }

    public void update(LocalTime sendTime, RepeatType repeatType, String daysOfWeek, Boolean isActive) {
        if (sendTime != null) {
            this.sendTime = sendTime;
        }
        if (repeatType != null) {
            this.repeatType = repeatType;
        }
        if (daysOfWeek != null) {
            this.daysOfWeek = daysOfWeek;
        }
        if (isActive != null) {
            this.isActive = isActive;
        }
    }

    public void markSent(LocalDateTime sentAt) {
        this.lastSentAt = sentAt;
    }
}
