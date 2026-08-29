package com.galpi.galpibackend.domain.devicetoken.controller;

import com.galpi.galpibackend.domain.devicetoken.dto.DeviceTokenRequest;
import com.galpi.galpibackend.domain.devicetoken.service.DeviceTokenService;
import com.galpi.galpibackend.global.security.CurrentUserId;
import com.galpi.galpibackend.global.web.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "디바이스 토큰", description = "푸시 알림 발송을 위한 FCM 디바이스 토큰 등록.")
@RestController
@RequestMapping("/api/device-tokens")
public class DeviceTokenController {

    private final DeviceTokenService deviceTokenService;

    public DeviceTokenController(DeviceTokenService deviceTokenService) {
        this.deviceTokenService = deviceTokenService;
    }

    @Operation(summary = "FCM 토큰 등록",
            description = "앱이 발급받은 FCM 토큰을 등록합니다. 같은 토큰을 다시 보내면 소유자/플랫폼만 갱신됩니다(upsert).")
    @PostMapping
    public ResponseEntity<SuccessResponse> register(@CurrentUserId Long userId,
                                                     @Valid @RequestBody DeviceTokenRequest request) {
        return ResponseEntity.ok(deviceTokenService.register(userId, request));
    }
}
