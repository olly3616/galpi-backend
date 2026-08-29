package com.galpi.galpibackend.domain.work.controller;

import com.galpi.galpibackend.domain.quote.dto.WorkQuotesResponse;
import com.galpi.galpibackend.domain.quote.service.QuoteService;
import com.galpi.galpibackend.domain.work.dto.WorkResponse;
import com.galpi.galpibackend.domain.work.service.WorkService;
import com.galpi.galpibackend.global.security.CurrentUserId;
import com.galpi.galpibackend.global.web.ApiPaging;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "책", description = "책 상세 정보와 '책 상세=대사 모아보기'. workId는 책의 ID입니다.")
@RestController
@RequestMapping("/api/works")
@Validated
public class WorkController {

    private final WorkService workService;
    private final QuoteService quoteService;

    public WorkController(WorkService workService, QuoteService quoteService) {
        this.workService = workService;
        this.quoteService = quoteService;
    }

    @Operation(summary = "책 상세 정보", description = "책 한 권의 기본 정보(제목·저자·표지·유형·출처)를 조회합니다.")
    @GetMapping("/{workId}")
    public ResponseEntity<WorkResponse> getWork(@Parameter(description = "책 ID") @PathVariable Long workId) {
        return ResponseEntity.ok(workService.getWork(workId));
    }

    @Operation(summary = "책 상세 = 대사 모아보기",
            description = "그 책에 내가 기록한 대사를 페이지네이션해 반환합니다. 응답은 { work, quotes: {items,page,hasNext} }.")
    @GetMapping("/{workId}/quotes")
    public ResponseEntity<WorkQuotesResponse> getWorkQuotes(
            @CurrentUserId Long userId,
            @Parameter(description = "책 ID") @PathVariable Long workId,
            @Parameter(description = "페이지 번호(0부터)") @RequestParam(defaultValue = ApiPaging.DEFAULT_PAGE) @Min(0) int page,
            @Parameter(description = "페이지당 개수") @RequestParam(defaultValue = ApiPaging.DEFAULT_SIZE) @Min(1) int size) {
        return ResponseEntity.ok(quoteService.getWorkQuotes(userId, workId, page, size));
    }
}
