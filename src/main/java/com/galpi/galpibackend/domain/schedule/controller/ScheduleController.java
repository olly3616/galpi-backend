package com.galpi.galpibackend.domain.schedule.controller;

import com.galpi.galpibackend.domain.schedule.dto.CreateScheduleRequest;
import com.galpi.galpibackend.domain.schedule.dto.DeleteScheduleResponse;
import com.galpi.galpibackend.domain.schedule.dto.ScheduleResponse;
import com.galpi.galpibackend.domain.schedule.dto.ScheduleWithQuoteResponse;
import com.galpi.galpibackend.domain.schedule.dto.UpdateScheduleRequest;
import com.galpi.galpibackend.domain.schedule.service.ScheduleService;
import com.galpi.galpibackend.global.security.CurrentUserId;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @PostMapping("/api/quotes/{quoteId}/schedules")
    public ResponseEntity<ScheduleResponse> createSchedule(@CurrentUserId Long userId,
                                                           @PathVariable Long quoteId,
                                                           @Valid @RequestBody CreateScheduleRequest request) {
        ScheduleResponse response = scheduleService.createSchedule(userId, quoteId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/schedules/me")
    public ResponseEntity<List<ScheduleWithQuoteResponse>> getMySchedules(@CurrentUserId Long userId) {
        return ResponseEntity.ok(scheduleService.getMySchedules(userId));
    }

    @PatchMapping("/api/schedules/{id}")
    public ResponseEntity<ScheduleResponse> updateSchedule(@CurrentUserId Long userId,
                                                           @PathVariable Long id,
                                                           @Valid @RequestBody UpdateScheduleRequest request) {
        ScheduleResponse response = scheduleService.updateSchedule(userId, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/api/schedules/{id}")
    public ResponseEntity<DeleteScheduleResponse> deleteSchedule(@CurrentUserId Long userId,
                                                                 @PathVariable Long id) {
        DeleteScheduleResponse response = scheduleService.deleteSchedule(userId, id);
        return ResponseEntity.ok(response);
    }
}
