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
        // 같은 토큰이 이미 있으면 소유자/플랫폼만 갱신, 없으면 새로 저장 (upsert)
        deviceTokenRepository.findByToken(request.token())
                .ifPresentOrElse(
                        existing -> existing.updateOwner(userId, request.platform()),
                        () -> deviceTokenRepository.save(DeviceToken.builder()
                                .userId(userId)
                                .token(request.token())
                                .platform(request.platform())
                                .build())
                );

        return SuccessResponse.ok();
    }
}
