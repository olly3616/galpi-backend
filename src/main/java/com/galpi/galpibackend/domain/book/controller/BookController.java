package com.galpi.galpibackend.domain.book.controller;

import com.galpi.galpibackend.domain.book.dto.BookItem;
import com.galpi.galpibackend.domain.book.service.BookSearchService;
import com.galpi.galpibackend.global.web.ApiPaging;
import com.galpi.galpibackend.global.web.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "도서 검색", description = "카카오 도서 API를 서버가 프록시하는 검색. 결과의 책을 책장에 추가할 수 있습니다.")
@RestController
@RequestMapping("/api/books")
@Validated
public class BookController {

    private final BookSearchService bookSearchService;

    public BookController(BookSearchService bookSearchService) {
        this.bookSearchService = bookSearchService;
    }

    @Operation(summary = "도서 검색", description = "검색어로 책을 찾습니다. 결과에는 제목·저자·출판사·표지·ISBN이 포함됩니다.")
    @GetMapping("/search")
    public ResponseEntity<PageResponse<BookItem>> search(
            @Parameter(description = "검색어(책 제목 등)") @RequestParam String query,
            @Parameter(description = "페이지 번호(0부터)") @RequestParam(defaultValue = ApiPaging.DEFAULT_PAGE) @Min(0) int page,
            @Parameter(description = "페이지당 개수") @RequestParam(defaultValue = ApiPaging.DEFAULT_SIZE) @Min(1) int size) {
        return ResponseEntity.ok(bookSearchService.search(query, page, size));
    }
}
