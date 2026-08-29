package com.galpi.galpibackend.domain.schedule.controller;

import com.galpi.galpibackend.domain.schedule.dto.CreateScheduleRequest;
import com.galpi.galpibackend.domain.schedule.dto.ScheduleResponse;
import com.galpi.galpibackend.domain.schedule.dto.ScheduleWithQuoteResponse;
import com.galpi.galpibackend.domain.schedule.dto.UpdateScheduleRequest;
import com.galpi.galpibackend.domain.schedule.service.ScheduleService;
import com.galpi.galpibackend.global.security.CurrentUserId;
import com.galpi.galpibackend.global.web.ApiPaging;
import com.galpi.galpibackend.global.web.PageResponse;
import com.galpi.galpibackend.global.web.SuccessResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @PostMapping("/api/quotes/{id}/schedules")
    public ResponseEntity<ScheduleResponse> createSchedule(@CurrentUserId Long userId,
                                                           @PathVariable Long id,
                                                           @Valid @RequestBody CreateScheduleRequest request) {
        ScheduleResponse response = scheduleService.createSchedule(userId, id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/schedules/me")
    public ResponseEntity<PageResponse<ScheduleWithQuoteResponse>> getMySchedules(
            @CurrentUserId Long userId,
            @RequestParam(defaultValue = ApiPaging.DEFAULT_PAGE) @Min(0) int page,
            @RequestParam(defaultValue = ApiPaging.DEFAULT_SIZE) @Min(1) int size) {
        return ResponseEntity.ok(scheduleService.getMySchedules(userId, page, size));
    }

    @PatchMapping("/api/schedules/{id}")
    public ResponseEntity<ScheduleResponse> updateSchedule(@CurrentUserId Long userId,
                                                           @PathVariable Long id,
                                                           @Valid @RequestBody UpdateScheduleRequest request) {
        ScheduleResponse response = scheduleService.updateSchedule(userId, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/api/schedules/{id}")
    public ResponseEntity<SuccessResponse> deleteSchedule(@CurrentUserId Long userId,
                                                          @PathVariable Long id) {
        return ResponseEntity.ok(scheduleService.deleteSchedule(userId, id));
    }
}
