package com.galpi.galpibackend.domain.bookshelf.controller;

import com.galpi.galpibackend.domain.bookshelf.dto.AddBookshelfRequest;
import com.galpi.galpibackend.domain.bookshelf.dto.AddBookshelfResponse;
import com.galpi.galpibackend.domain.bookshelf.dto.BookshelfItem;
import com.galpi.galpibackend.domain.bookshelf.service.BookshelfService;
import com.galpi.galpibackend.global.security.CurrentUserId;
import com.galpi.galpibackend.global.web.ApiPaging;
import com.galpi.galpibackend.global.web.PageResponse;
import com.galpi.galpibackend.global.web.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "책장", description = "내 책장에 책 추가/조회/빼기. 검색한 책(API) 또는 직접 등록(MANUAL) 모두 추가할 수 있습니다.")
@RestController
@RequestMapping("/api/bookshelf")
@Validated
public class BookshelfController {

    private final BookshelfService bookshelfService;

    public BookshelfController(BookshelfService bookshelfService) {
        this.bookshelfService = bookshelfService;
    }

    @Operation(summary = "책장에 책 추가",
            description = "검색한 책(source=API) 또는 직접 등록(source=MANUAL)을 책장에 담습니다. 이미 있으면 409 ALREADY_IN_SHELF.")
    @PostMapping
    public ResponseEntity<AddBookshelfResponse> addBook(@CurrentUserId Long userId,
                                                        @Valid @RequestBody AddBookshelfRequest request) {
        AddBookshelfResponse response = bookshelfService.addBook(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "내 책장 조회", description = "내가 꽂은 책을 최근 추가순으로 페이지네이션해 반환합니다(각 책의 대사 수 포함).")
    @GetMapping("/me")
    public ResponseEntity<PageResponse<BookshelfItem>> getMyBookshelf(
            @CurrentUserId Long userId,
            @Parameter(description = "페이지 번호(0부터)") @RequestParam(defaultValue = ApiPaging.DEFAULT_PAGE) @Min(0) int page,
            @Parameter(description = "페이지당 개수") @RequestParam(defaultValue = ApiPaging.DEFAULT_SIZE) @Min(1) int size) {
        return ResponseEntity.ok(bookshelfService.getMyBookshelf(userId, page, size));
    }

    @Operation(summary = "책장에서 빼기", description = "책장에서 해당 책을 제거합니다(대사는 유지됩니다). 본인 책장만.")
    @DeleteMapping("/{workId}")
    public ResponseEntity<SuccessResponse> removeBook(
            @CurrentUserId Long userId,
            @Parameter(description = "책 ID") @PathVariable Long workId) {
        return ResponseEntity.ok(bookshelfService.removeBook(userId, workId));
    }
}
