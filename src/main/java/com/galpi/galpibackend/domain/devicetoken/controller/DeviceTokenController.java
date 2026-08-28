package com.galpi.galpibackend.domain.devicetoken.controller;

import com.galpi.galpibackend.domain.devicetoken.dto.DeviceTokenRequest;
import com.galpi.galpibackend.domain.devicetoken.dto.DeviceTokenResponse;
import com.galpi.galpibackend.domain.devicetoken.service.DeviceTokenService;
import com.galpi.galpibackend.global.security.CurrentUserId;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/device-tokens")
public class DeviceTokenController {

    private final DeviceTokenService deviceTokenService;

    public DeviceTokenController(DeviceTokenService deviceTokenService) {
        this.deviceTokenService = deviceTokenService;
    }

    @PostMapping
    public ResponseEntity<DeviceTokenResponse> register(@CurrentUserId Long userId,
                                                        @Valid @RequestBody DeviceTokenRequest request) {
        DeviceTokenResponse response = deviceTokenService.register(userId, request);
        return ResponseEntity.ok(response);
    }
}
