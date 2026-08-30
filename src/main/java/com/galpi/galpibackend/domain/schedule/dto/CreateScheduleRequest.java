package com.galpi.galpibackend.domain.schedule.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.galpi.galpibackend.domain.schedule.entity.RepeatType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public record CreateScheduleRequest(
        @Schema(description = "발송 시각 (HH:mm)", example = "08:00", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "발송 시각은 필수입니다.")
        @JsonFormat(pattern = "HH:mm")
        LocalTime sendTime,

        @Schema(description = "반복 유형", example = "DAILY", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "반복 유형은 필수입니다.")
        RepeatType repeatType,

        @Schema(description = "WEEKLY일 때 요일들 (MON~SUN, 콤마 구분). WEEKLY면 필수",
                example = "MON,WED,FRI", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String daysOfWeek,

        @Schema(description = "ONCE일 때 발송할 날짜 (yyyy-MM-dd). ONCE면 필수",
                example = "2026-09-15", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        LocalDate sendDate
) {
}
