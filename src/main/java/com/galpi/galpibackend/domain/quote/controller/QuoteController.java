package com.galpi.galpibackend.domain.quote.controller;

import com.galpi.galpibackend.domain.quote.dto.CreateQuoteRequest;
import com.galpi.galpibackend.domain.quote.dto.QuoteResponse;
import com.galpi.galpibackend.domain.quote.dto.UpdateQuoteRequest;
import com.galpi.galpibackend.domain.quote.service.QuoteService;
import com.galpi.galpibackend.global.security.CurrentUserId;
import com.galpi.galpibackend.global.web.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "대사", description = "책에 기록한 구절(대사)의 작성·조회·수정·삭제. 조회/수정/삭제는 본인 대사만 가능합니다.")
@RestController
@RequestMapping("/api/quotes")
public class QuoteController {

    private final QuoteService quoteService;

    public QuoteController(QuoteService quoteService) {
        this.quoteService = quoteService;
    }

    @Operation(summary = "대사 작성", description = "특정 책(workId)에 대사를 기록합니다. visibility 미지정 시 PRIVATE로 저장됩니다.")
    @ApiResponse(responseCode = "201", description = "작성 성공, 생성된 대사 반환")
    @PostMapping
    public ResponseEntity<QuoteResponse> createQuote(@CurrentUserId Long userId,
                                                     @Valid @RequestBody CreateQuoteRequest request) {
        QuoteResponse response = quoteService.createQuote(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "대사 상세 조회", description = "대사 하나의 상세(출처·설정된 알림 포함)를 조회합니다. 본인 대사만 가능합니다.")
    @GetMapping("/{quoteId}")
    public ResponseEntity<QuoteResponse> getQuote(@CurrentUserId Long userId,
                                                  @Parameter(description = "대사 ID") @PathVariable Long quoteId) {
        QuoteResponse response = quoteService.getQuote(userId, quoteId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "대사 수정", description = "대사를 부분 수정합니다. 전달한 필드만 변경됩니다. 본인 대사만 가능합니다.")
    @PatchMapping("/{quoteId}")
    public ResponseEntity<QuoteResponse> updateQuote(@CurrentUserId Long userId,
                                                     @Parameter(description = "대사 ID") @PathVariable Long quoteId,
                                                     @Valid @RequestBody UpdateQuoteRequest request) {
        QuoteResponse response = quoteService.updateQuote(userId, quoteId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "대사 삭제", description = "대사를 삭제(소프트 딜리트)합니다. 연결된 알림·좋아요도 함께 삭제됩니다. 본인 대사만 가능합니다.")
    @DeleteMapping("/{quoteId}")
    public ResponseEntity<SuccessResponse> deleteQuote(@CurrentUserId Long userId,
                                                       @Parameter(description = "대사 ID") @PathVariable Long quoteId) {
        return ResponseEntity.ok(quoteService.deleteQuote(userId, quoteId));
    }
}
