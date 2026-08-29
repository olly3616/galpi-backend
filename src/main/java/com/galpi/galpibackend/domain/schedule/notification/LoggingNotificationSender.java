package com.galpi.galpibackend.domain.schedule.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * FCM 연동 전까지 사용하는 임시 구현. 발송 대상과 내용을 로그로만 남긴다.
 * 추후 FcmNotificationSender로 교체 예정.
 */
@Component
public class LoggingNotificationSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(LoggingNotificationSender.class);

    @Override
    public void send(Long userId, String title, String body, Long quoteId) {
        // 대사 본문(개인 콘텐츠)은 INFO 로그에 남기지 않는다. 상세는 DEBUG에서만.
        log.info("[알림 발송] userId={}, quoteId={}, title={}", userId, quoteId, title);
        log.debug("[알림 발송 상세] quoteId={}, body={}", quoteId, body);
    }
}
