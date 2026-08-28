package com.galpi.galpibackend.domain.schedule.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.galpi.galpibackend.domain.schedule.entity.RepeatType;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

public record CreateScheduleRequest(
        @NotNull(message = "발송 시각은 필수입니다.")
        @JsonFormat(pattern = "HH:mm")
        LocalTime sendTime,

        @NotNull(message = "반복 유형은 필수입니다.")
        RepeatType repeatType,

        // WEEKLY일 때 "MON,WED,FRI"
        String daysOfWeek
) {
}
