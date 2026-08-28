package com.galpi.galpibackend.domain.schedule.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.galpi.galpibackend.domain.schedule.entity.RepeatType;
import java.time.LocalTime;

/**
 * 부분 수정(PATCH). null이 아닌 필드만 변경한다.
 */
public record UpdateScheduleRequest(
        @JsonFormat(pattern = "HH:mm")
        LocalTime sendTime,

        RepeatType repeatType,

        String daysOfWeek,

        Boolean isActive
) {
}
