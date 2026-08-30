package com.galpi.galpibackend.domain.schedule.notification;

import com.galpi.galpibackend.domain.devicetoken.entity.DeviceToken;
import com.galpi.galpibackend.domain.devicetoken.repository.DeviceTokenRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 실제 FCM 발송 구현. fcm.enabled=true일 때만 활성화된다.
 * 수신자의 모든 디바이스 토큰으로 발송하며, 한 토큰 실패가 나머지를 막지 않는다.
 */
@Component
@ConditionalOnProperty(name = "fcm.enabled", havingValue = "true")
public class FcmNotificationSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(FcmNotificationSender.class);

    private final FirebaseMessaging firebaseMessaging;
    private final DeviceTokenRepository deviceTokenRepository;

    public FcmNotificationSender(FirebaseMessaging firebaseMessaging,
                                 DeviceTokenRepository deviceTokenRepository) {
        this.firebaseMessaging = firebaseMessaging;
        this.deviceTokenRepository = deviceTokenRepository;
    }

    @Override
    public void send(Long userId, String title, String body, Long quoteId) {
        List<DeviceToken> tokens = deviceTokenRepository.findByUserId(userId);
        if (tokens.isEmpty()) {
            log.debug("[FCM] 등록된 디바이스 토큰 없음 (userId={})", userId);
            return;
        }
        for (DeviceToken deviceToken : tokens) {
            Message message = Message.builder()
                    .setToken(deviceToken.getToken())
                    .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                    // 앱에서 딥링크로 해당 대사를 열 수 있도록 quoteId 전달
                    .putData("quoteId", String.valueOf(quoteId))
                    .build();
            try {
                firebaseMessaging.send(message);
            } catch (FirebaseMessagingException e) {
                // 개별 토큰 실패(만료·무효 등)는 로깅만 하고 계속 진행한다.
                log.warn("[FCM] 발송 실패 (userId={}, tokenId={}): {}",
                        userId, deviceToken.getId(), e.getMessage());
            }
        }
    }
}
