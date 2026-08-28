package com.galpi.galpibackend.domain.schedule.notification;

/**
 * 대사 알림 발송 추상화. 현재는 로깅 구현이며, 추후 FCM 구현으로 교체한다.
 */
public interface NotificationSender {

    /**
     * @param userId  수신 사용자 (구현체가 device_tokens를 조회해 발송)
     * @param title   알림 제목 (책 제목 등)
     * @param body    알림 본문 (대사 내용)
     * @param quoteId 딥링크 대상 대사 ID
     */
    void send(Long userId, String title, String body, Long quoteId);
}
