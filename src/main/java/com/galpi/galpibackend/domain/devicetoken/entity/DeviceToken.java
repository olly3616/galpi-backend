package com.galpi.galpibackend.domain.devicetoken.entity;

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
@Table(name = "device_tokens")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeviceToken extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, unique = true, length = 512)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Platform platform;

    @Builder
    private DeviceToken(Long userId, String token, Platform platform) {
        this.userId = userId;
        this.token = token;
        this.platform = platform;
    }

    public boolean isOwnedBy(Long userId) {
        return this.userId.equals(userId);
    }

    /**
     * 이미 등록된 토큰이 다른 사용자/플랫폼으로 재사용될 때 소유자 정보를 갱신한다.
     * (같은 기기에서 다른 계정으로 재로그인하는 경우)
     */
    public void updateOwner(Long userId, Platform platform) {
        this.userId = userId;
        this.platform = platform;
    }
}
