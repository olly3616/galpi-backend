package com.galpi.galpibackend.domain.book.controller;

import com.galpi.galpibackend.domain.book.dto.BookSearchResponse;
import com.galpi.galpibackend.domain.book.service.BookSearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookSearchService bookSearchService;

    public BookController(BookSearchService bookSearchService) {
        this.bookSearchService = bookSearchService;
    }

    @GetMapping("/search")
    public ResponseEntity<BookSearchResponse> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        BookSearchResponse response = bookSearchService.search(query, page, size);
        return ResponseEntity.ok(response);
    }
}
