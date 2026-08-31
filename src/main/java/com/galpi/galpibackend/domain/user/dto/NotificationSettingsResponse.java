package com.galpi.galpibackend.domain.user.dto;

import com.galpi.galpibackend.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 알림 설정 조회/수정 응답.
 */
public record NotificationSettingsResponse(
        @Schema(description = "예약 문장 푸시 알림 수신 여부(기본 true)") boolean quoteAlarm,
        @Schema(description = "마케팅·소식 알림 수신 여부(기본 false)") boolean marketing
) {

    public static NotificationSettingsResponse from(User user) {
        return new NotificationSettingsResponse(user.isQuoteAlarm(), user.isMarketing());
    }
}
