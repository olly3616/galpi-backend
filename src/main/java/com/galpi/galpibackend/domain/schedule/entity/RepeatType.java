package com.galpi.galpibackend.domain.schedule.entity;

public enum RepeatType {
    DAILY,    // 매일
    WEEKLY,   // 특정 요일 반복 (daysOfWeek 사용)
    ONCE      // 한 번만
}
