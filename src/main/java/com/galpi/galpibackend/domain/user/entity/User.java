package com.galpi.galpibackend.domain.user.entity;

import com.galpi.galpibackend.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, unique = true, length = 20)
    private String nickname;

    @Column(length = 500)
    private String bio;

    @Column(length = 1024)
    private String profileImageUrl;

    // 예약 문장 푸시 알림 수신 여부(기본 켜짐). false면 발송 로직에서 제외된다.
    @Column(nullable = false)
    private boolean quoteAlarm = true;

    // 마케팅·소식 알림 수신 여부(기본 꺼짐).
    @Column(nullable = false)
    private boolean marketing = false;

    @Builder
    private User(String email, String password, String nickname, String bio, String profileImageUrl) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.bio = bio;
        this.profileImageUrl = profileImageUrl;
    }

    /**
     * 알림 설정 부분 수정. null이 아닌 항목만 변경한다.
     */
    public void updateNotificationSettings(Boolean quoteAlarm, Boolean marketing) {
        if (quoteAlarm != null) {
            this.quoteAlarm = quoteAlarm;
        }
        if (marketing != null) {
            this.marketing = marketing;
        }
    }

    /**
     * 프로필 부분 수정. null이 아닌 필드만 변경한다(닉네임 중복 검사는 서비스에서 수행).
     */
    public void updateProfile(String nickname, String bio, String profileImageUrl) {
        if (nickname != null) {
            this.nickname = nickname;
        }
        if (bio != null) {
            this.bio = bio;
        }
        if (profileImageUrl != null) {
            this.profileImageUrl = profileImageUrl;
        }
    }
}
