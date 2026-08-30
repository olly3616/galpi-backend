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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "예약 알림", description = "대사를 특정 시각에 다시 만나도록 알림을 예약합니다. 설정 시각에 서버가 푸시를 발송합니다.")
@RestController
@Validated
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @Operation(summary = "대사 알림 생성",
            description = "대사(quoteId)에 알림을 설정합니다. repeatType이 WEEKLY이면 daysOfWeek가 필수입니다. 본인 대사만 가능.")
    @ApiResponse(responseCode = "201", description = "생성 성공, 생성된 알림 반환")
    @PostMapping("/api/quotes/{quoteId}/schedules")
    public ResponseEntity<ScheduleResponse> createSchedule(@CurrentUserId Long userId,
                                                           @Parameter(description = "대사 ID") @PathVariable Long quoteId,
                                                           @Valid @RequestBody CreateScheduleRequest request) {
        ScheduleResponse response = scheduleService.createSchedule(userId, quoteId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "내 알림 목록", description = "내가 설정한 알림을 대사 정보와 함께 페이지네이션해 반환합니다.")
    @GetMapping("/api/schedules/me")
    public ResponseEntity<PageResponse<ScheduleWithQuoteResponse>> getMySchedules(
            @CurrentUserId Long userId,
            @Parameter(description = "페이지 번호(0부터)") @RequestParam(defaultValue = ApiPaging.DEFAULT_PAGE) @Min(0) int page,
            @Parameter(description = "페이지당 개수") @RequestParam(defaultValue = ApiPaging.DEFAULT_SIZE) @Min(1) int size) {
        return ResponseEntity.ok(scheduleService.getMySchedules(userId, page, size));
    }

    @Operation(summary = "알림 수정", description = "알림을 부분 수정합니다(시간/반복/on-off). 본인 알림만 가능.")
    @PatchMapping("/api/schedules/{scheduleId}")
    public ResponseEntity<ScheduleResponse> updateSchedule(
            @CurrentUserId Long userId,
            @Parameter(description = "알림 ID") @PathVariable Long scheduleId,
            @Valid @RequestBody UpdateScheduleRequest request) {
        ScheduleResponse response = scheduleService.updateSchedule(userId, scheduleId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "알림 삭제", description = "알림을 삭제합니다. 본인 알림만 가능.")
    @DeleteMapping("/api/schedules/{scheduleId}")
    public ResponseEntity<SuccessResponse> deleteSchedule(
            @CurrentUserId Long userId,
            @Parameter(description = "알림 ID") @PathVariable Long scheduleId) {
        return ResponseEntity.ok(scheduleService.deleteSchedule(userId, scheduleId));
    }
}
