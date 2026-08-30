package com.galpi.galpibackend.domain.devicetoken.service;

import com.galpi.galpibackend.domain.devicetoken.dto.DeviceTokenRequest;
import com.galpi.galpibackend.domain.devicetoken.entity.DeviceToken;
import com.galpi.galpibackend.domain.devicetoken.repository.DeviceTokenRepository;
import com.galpi.galpibackend.global.web.SuccessResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeviceTokenService {

    private final DeviceTokenRepository deviceTokenRepository;

    public DeviceTokenService(DeviceTokenRepository deviceTokenRepository) {
        this.deviceTokenRepository = deviceTokenRepository;
    }

    @Transactional
    public SuccessResponse register(Long userId, DeviceTokenRequest request) {
        // upsert: 같은 토큰이 이미 있으면 소유자/플랫폼을 호출자로 갱신, 없으면 새로 저장한다.
        //
        // 소유자 이전은 의도된 동작이다. FCM 토큰은 '기기' 단위이므로, 같은 기기에서
        // 다른 계정으로 재로그인하면 그 기기의 알림 대상도 새 계정이 되어야 한다.
        // (이전을 막으면 이전 계정의 사적 알림이 새 사용자의 기기로 계속 배송되어 오히려 위험하다.)
        // 다만 탈취한 토큰으로 남의 알림 라우팅을 흔드는 남용은 이 계층에서 막을 수 없고,
        // 엔드포인트 레이트리밋으로 다룬다.
        deviceTokenRepository.findByToken(request.token())
                .ifPresentOrElse(
                        existing -> {
                            // 이미 같은 소유자·플랫폼이면 불필요한 UPDATE를 피한다.
                            if (!existing.isOwnedBy(userId) || existing.getPlatform() != request.platform()) {
                                existing.updateOwner(userId, request.platform());
                            }
                        },
                        () -> deviceTokenRepository.save(DeviceToken.builder()
                                .userId(userId)
                                .token(request.token())
                                .platform(request.platform())
                                .build())
                );

        return SuccessResponse.ok();
    }
}
