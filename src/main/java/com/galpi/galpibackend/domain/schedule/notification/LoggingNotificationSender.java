package com.galpi.galpibackend.domain.schedule.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * FCM 비활성(fcm.enabled=false, 기본) 시 사용하는 구현. 발송 대상과 내용을 로그로만 남긴다.
 * fcm.enabled=true이면 {@link FcmNotificationSender}가 대신 활성화된다.
 */
@Component
@ConditionalOnProperty(name = "fcm.enabled", havingValue = "false", matchIfMissing = true)
public class LoggingNotificationSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(LoggingNotificationSender.class);

    @Override
    public void send(Long userId, String title, String body, Long quoteId) {
        // 대사 본문(개인 콘텐츠)은 INFO 로그에 남기지 않는다. 상세는 DEBUG에서만.
        log.info("[알림 발송] userId={}, quoteId={}, title={}", userId, quoteId, title);
        log.debug("[알림 발송 상세] quoteId={}, body={}", quoteId, body);
    }
}
