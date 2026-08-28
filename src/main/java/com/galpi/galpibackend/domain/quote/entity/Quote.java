package com.galpi.galpibackend.domain.quote.entity;

import com.galpi.galpibackend.domain.work.entity.Work;
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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "quotes")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Quote extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "work_id", nullable = false)
    private Work work;

    @Column(length = 100)
    private String characterName;

    @Column(nullable = false, length = 5000)
    private String content;

    @Column(length = 2000)
    private String memo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Visibility visibility;

    @Builder
    private Quote(Long userId, Work work, String characterName, String content,
                  String memo, Visibility visibility) {
        this.userId = userId;
        this.work = work;
        this.characterName = characterName;
        this.content = content;
        this.memo = memo;
        this.visibility = visibility;
    }

    public boolean isOwnedBy(Long userId) {
        return this.userId.equals(userId);
    }

    public void changeContent(String content) {
        this.content = content;
    }

    public void changeMemo(String memo) {
        this.memo = memo;
    }

    public void changeCharacterName(String characterName) {
        this.characterName = characterName;
    }

    public void changeVisibility(Visibility visibility) {
        this.visibility = visibility;
    }
}
