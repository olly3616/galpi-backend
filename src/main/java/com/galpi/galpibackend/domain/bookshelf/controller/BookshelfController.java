package com.galpi.galpibackend.domain.bookshelf.controller;

import com.galpi.galpibackend.domain.bookshelf.dto.AddBookshelfRequest;
import com.galpi.galpibackend.domain.bookshelf.dto.AddBookshelfResponse;
import com.galpi.galpibackend.domain.bookshelf.dto.BookshelfResponse;
import com.galpi.galpibackend.domain.bookshelf.dto.RemoveBookshelfResponse;
import com.galpi.galpibackend.domain.bookshelf.service.BookshelfService;
import com.galpi.galpibackend.global.security.CurrentUserId;
import com.galpi.galpibackend.global.web.ApiPaging;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookshelf")
@Validated
public class BookshelfController {

    private final BookshelfService bookshelfService;

    public BookshelfController(BookshelfService bookshelfService) {
        this.bookshelfService = bookshelfService;
    }

    @PostMapping
    public ResponseEntity<AddBookshelfResponse> addBook(@CurrentUserId Long userId,
                                                        @Valid @RequestBody AddBookshelfRequest request) {
        AddBookshelfResponse response = bookshelfService.addBook(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<BookshelfResponse> getMyBookshelf(
            @CurrentUserId Long userId,
            @RequestParam(defaultValue = ApiPaging.DEFAULT_PAGE) @Min(0) int page,
            @RequestParam(defaultValue = ApiPaging.DEFAULT_SIZE) @Min(1) int size) {
        BookshelfResponse response = bookshelfService.getMyBookshelf(userId, page, size);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{workId}")
    public ResponseEntity<RemoveBookshelfResponse> removeBook(@CurrentUserId Long userId,
                                                             @PathVariable Long workId) {
        RemoveBookshelfResponse response = bookshelfService.removeBook(userId, workId);
        return ResponseEntity.ok(response);
    }
}
