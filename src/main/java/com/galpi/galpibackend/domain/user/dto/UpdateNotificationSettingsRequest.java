package com.galpi.galpibackend.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 알림 설정 부분 수정(PATCH). null이 아닌 항목만 변경한다. (모든 필드 선택)
 */
public record UpdateNotificationSettingsRequest(
        @Schema(description = "예약 문장 푸시 알림 수신 여부", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Boolean quoteAlarm,

        @Schema(description = "마케팅·소식 알림 수신 여부", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Boolean marketing
) {
}
