package com.galpi.galpibackend.domain.schedule.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.galpi.galpibackend.domain.schedule.entity.RepeatType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 부분 수정(PATCH). null이 아닌 필드만 변경한다. (모든 필드 선택)
 */
public record UpdateScheduleRequest(
        @Schema(description = "발송 시각 HH:mm (선택)", example = "09:00", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @JsonFormat(pattern = "HH:mm")
        LocalTime sendTime,

        @Schema(description = "반복 유형(선택)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        RepeatType repeatType,

        @Schema(description = "요일들 (선택). 최종 상태가 WEEKLY면 필수", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String daysOfWeek,

        @Schema(description = "발송 날짜 yyyy-MM-dd (선택). 최종 상태가 ONCE면 필수",
                example = "2026-09-15", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        LocalDate sendDate,

        @Schema(description = "알림 on/off (선택)", example = "false", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Boolean isActive
) {
}
