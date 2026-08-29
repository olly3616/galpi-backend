package com.galpi.galpibackend.domain.devicetoken.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.galpi.galpibackend.domain.devicetoken.dto.DeviceTokenRequest;
import com.galpi.galpibackend.global.web.SuccessResponse;
import com.galpi.galpibackend.domain.devicetoken.entity.DeviceToken;
import com.galpi.galpibackend.domain.devicetoken.entity.Platform;
import com.galpi.galpibackend.domain.devicetoken.repository.DeviceTokenRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeviceTokenServiceTest {

    @Mock
    private DeviceTokenRepository deviceTokenRepository;

    @InjectMocks
    private DeviceTokenService deviceTokenService;

    private final DeviceTokenRequest request = new DeviceTokenRequest("fcm-token-abc", Platform.ANDROID);

    @Test
    @DisplayName("새 토큰이면 새로 저장한다")
    void register_newToken() {
        given(deviceTokenRepository.findByToken(request.token())).willReturn(Optional.empty());

        SuccessResponse response = deviceTokenService.register(1L, request);

        assertThat(response.success()).isTrue();
        verify(deviceTokenRepository).save(any(DeviceToken.class));
    }

    @Test
    @DisplayName("이미 있는 토큰이면 소유자만 갱신하고 새로 저장하지 않는다")
    void register_existingToken() {
        DeviceToken existing = DeviceToken.builder()
                .userId(2L)
                .token(request.token())
                .platform(Platform.IOS)
                .build();
        given(deviceTokenRepository.findByToken(request.token())).willReturn(Optional.of(existing));

        SuccessResponse response = deviceTokenService.register(1L, request);

        assertThat(response.success()).isTrue();
        assertThat(existing.getUserId()).isEqualTo(1L);
        assertThat(existing.getPlatform()).isEqualTo(Platform.ANDROID);
        verify(deviceTokenRepository, never()).save(any(DeviceToken.class));
    }
}
