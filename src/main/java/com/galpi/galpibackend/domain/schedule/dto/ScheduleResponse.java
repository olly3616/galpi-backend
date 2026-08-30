package com.galpi.galpibackend.domain.schedule.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.galpi.galpibackend.domain.schedule.entity.QuoteSchedule;
import com.galpi.galpibackend.domain.schedule.entity.RepeatType;
import java.time.LocalDate;
import java.time.LocalTime;

public record ScheduleResponse(
        Long scheduleId,
        @JsonFormat(pattern = "HH:mm")
        LocalTime sendTime,
        RepeatType repeatType,
        String daysOfWeek,
        LocalDate sendDate,
        boolean isActive
) {

    public static ScheduleResponse from(QuoteSchedule schedule) {
        return new ScheduleResponse(
                schedule.getId(),
                schedule.getSendTime(),
                schedule.getRepeatType(),
                schedule.getDaysOfWeek(),
                schedule.getSendDate(),
                schedule.isActive()
        );
    }
}
