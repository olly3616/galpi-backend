package com.galpi.galpibackend.domain.work.controller;

import com.galpi.galpibackend.domain.quote.dto.WorkQuotesResponse;
import com.galpi.galpibackend.domain.quote.service.QuoteService;
import com.galpi.galpibackend.domain.work.dto.WorkResponse;
import com.galpi.galpibackend.domain.work.service.WorkService;
import com.galpi.galpibackend.global.security.CurrentUserId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/works")
public class WorkController {

    private final WorkService workService;
    private final QuoteService quoteService;

    public WorkController(WorkService workService, QuoteService quoteService) {
        this.workService = workService;
        this.quoteService = quoteService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkResponse> getWork(@PathVariable Long id) {
        WorkResponse response = workService.getWork(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/quotes")
    public ResponseEntity<WorkQuotesResponse> getWorkQuotes(@CurrentUserId Long userId,
                                                            @PathVariable Long id) {
        WorkQuotesResponse response = quoteService.getWorkQuotes(userId, id);
        return ResponseEntity.ok(response);
    }
}
