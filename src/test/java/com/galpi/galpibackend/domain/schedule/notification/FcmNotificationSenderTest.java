package com.galpi.galpibackend.domain.schedule.notification;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.galpi.galpibackend.domain.devicetoken.entity.DeviceToken;
import com.galpi.galpibackend.domain.devicetoken.entity.Platform;
import com.galpi.galpibackend.domain.devicetoken.repository.DeviceTokenRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FcmNotificationSenderTest {

    @Mock
    private FirebaseMessaging firebaseMessaging;

    @Mock
    private DeviceTokenRepository deviceTokenRepository;

    @InjectMocks
    private FcmNotificationSender sender;

    private DeviceToken token(String value, Platform platform) {
        return DeviceToken.builder().userId(1L).token(value).platform(platform).build();
    }

    @Test
    @DisplayName("사용자의 모든 디바이스 토큰으로 발송한다")
    void send_toAllTokens() throws Exception {
        given(deviceTokenRepository.findByUserId(1L))
                .willReturn(List.of(token("tok1", Platform.ANDROID), token("tok2", Platform.IOS)));
        given(firebaseMessaging.send(any(Message.class))).willReturn("msg-id");

        sender.send(1L, "데미안", "새는 알에서...", 55L);

        verify(firebaseMessaging, times(2)).send(any(Message.class));
    }

    @Test
    @DisplayName("등록된 토큰이 없으면 발송하지 않는다")
    void send_noTokens() throws Exception {
        given(deviceTokenRepository.findByUserId(1L)).willReturn(List.of());

        sender.send(1L, "제목", "본문", 55L);

        verify(firebaseMessaging, never()).send(any(Message.class));
    }

    @Test
    @DisplayName("한 토큰 발송이 실패해도 나머지 토큰 발송은 계속된다")
    void send_continuesOnFailure() throws Exception {
        given(deviceTokenRepository.findByUserId(1L))
                .willReturn(List.of(token("tok1", Platform.ANDROID), token("tok2", Platform.IOS)));
        given(firebaseMessaging.send(any(Message.class)))
                .willThrow(mock(FirebaseMessagingException.class))
                .willReturn("msg-id");

        sender.send(1L, "데미안", "새는 알에서...", 55L);

        verify(firebaseMessaging, times(2)).send(any(Message.class));
    }
}
