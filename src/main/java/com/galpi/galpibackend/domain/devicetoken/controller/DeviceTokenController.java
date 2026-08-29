package com.galpi.galpibackend.domain.devicetoken.controller;

import com.galpi.galpibackend.domain.devicetoken.dto.DeviceTokenRequest;
import com.galpi.galpibackend.domain.devicetoken.service.DeviceTokenService;
import com.galpi.galpibackend.global.security.CurrentUserId;
import com.galpi.galpibackend.global.web.SuccessResponse;
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
    public ResponseEntity<SuccessResponse> register(@CurrentUserId Long userId,
                                                     @Valid @RequestBody DeviceTokenRequest request) {
        return ResponseEntity.ok(deviceTokenService.register(userId, request));
    }
}
