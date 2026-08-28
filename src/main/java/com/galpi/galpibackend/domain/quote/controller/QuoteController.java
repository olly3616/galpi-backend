package com.galpi.galpibackend.domain.quote.controller;

import com.galpi.galpibackend.domain.quote.dto.CreateQuoteRequest;
import com.galpi.galpibackend.domain.quote.dto.DeleteQuoteResponse;
import com.galpi.galpibackend.domain.quote.dto.QuoteResponse;
import com.galpi.galpibackend.domain.quote.dto.UpdateQuoteRequest;
import com.galpi.galpibackend.domain.quote.service.QuoteService;
import com.galpi.galpibackend.global.security.CurrentUserId;
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

@RestController
@RequestMapping("/api/quotes")
public class QuoteController {

    private final QuoteService quoteService;

    public QuoteController(QuoteService quoteService) {
        this.quoteService = quoteService;
    }

    @PostMapping
    public ResponseEntity<QuoteResponse> createQuote(@CurrentUserId Long userId,
                                                     @Valid @RequestBody CreateQuoteRequest request) {
        QuoteResponse response = quoteService.createQuote(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuoteResponse> getQuote(@CurrentUserId Long userId,
                                                  @PathVariable Long id) {
        QuoteResponse response = quoteService.getQuote(userId, id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<QuoteResponse> updateQuote(@CurrentUserId Long userId,
                                                     @PathVariable Long id,
                                                     @RequestBody UpdateQuoteRequest request) {
        QuoteResponse response = quoteService.updateQuote(userId, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<DeleteQuoteResponse> deleteQuote(@CurrentUserId Long userId,
                                                           @PathVariable Long id) {
        DeleteQuoteResponse response = quoteService.deleteQuote(userId, id);
        return ResponseEntity.ok(response);
    }
}
