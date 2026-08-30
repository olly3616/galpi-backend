package com.galpi.galpibackend.domain.bookshelf.service;

import com.galpi.galpibackend.domain.bookshelf.dto.AddBookshelfRequest;
import com.galpi.galpibackend.domain.bookshelf.dto.AddBookshelfResponse;
import com.galpi.galpibackend.domain.bookshelf.dto.BookshelfItem;
import com.galpi.galpibackend.domain.bookshelf.entity.Bookshelf;
import com.galpi.galpibackend.domain.bookshelf.repository.BookshelfRepository;
import com.galpi.galpibackend.domain.quote.repository.QuoteRepository;
import com.galpi.galpibackend.domain.quote.repository.QuoteRepository.WorkQuoteCount;
import com.galpi.galpibackend.domain.work.entity.BookSource;
import com.galpi.galpibackend.domain.work.entity.Work;
import com.galpi.galpibackend.domain.work.repository.WorkRepository;
import com.galpi.galpibackend.global.error.CustomException;
import com.galpi.galpibackend.global.error.ErrorCode;
import com.galpi.galpibackend.global.web.PageResponse;
import com.galpi.galpibackend.global.web.SuccessResponse;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class BookshelfService {

    private final BookshelfRepository bookshelfRepository;
    private final WorkRepository workRepository;
    private final QuoteRepository quoteRepository;

    public BookshelfService(BookshelfRepository bookshelfRepository, WorkRepository workRepository,
                            QuoteRepository quoteRepository) {
        this.bookshelfRepository = bookshelfRepository;
        this.workRepository = workRepository;
        this.quoteRepository = quoteRepository;
    }

    @Transactional
    public AddBookshelfResponse addBook(Long userId, AddBookshelfRequest request) {
        Work work = resolveWork(userId, request);

        if (bookshelfRepository.existsByUserIdAndWorkId(userId, work.getId())) {
            throw new CustomException(ErrorCode.ALREADY_IN_SHELF);
        }

        bookshelfRepository.save(Bookshelf.builder()
                .userId(userId)
                .work(work)
                .build());

        return new AddBookshelfResponse(work.getId());
    }

    @Transactional(readOnly = true)
    public PageResponse<BookshelfItem> getMyBookshelf(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Bookshelf> shelfPage = bookshelfRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        List<Long> workIds = shelfPage.getContent().stream()
                .map(shelf -> shelf.getWork().getId())
                .toList();
        Map<Long, Long> quoteCounts = countQuotesByWork(userId, workIds);

        var items = shelfPage.getContent().stream()
                .map(shelf -> {
                    Work work = shelf.getWork();
                    long quoteCount = quoteCounts.getOrDefault(work.getId(), 0L);
                    return new BookshelfItem(work.getId(), work.getTitle(), work.getAuthor(),
                            work.getCoverUrl(), quoteCount);
                })
                .toList();

        return PageResponse.from(shelfPage, items);
    }

    private Map<Long, Long> countQuotesByWork(Long userId, List<Long> workIds) {
        if (workIds.isEmpty()) {
            return Map.of();
        }
        return quoteRepository.countByUserIdAndWorkIdIn(userId, workIds).stream()
                .collect(Collectors.toMap(WorkQuoteCount::getWorkId, WorkQuoteCount::getCount));
    }

    @Transactional
    public SuccessResponse removeBook(Long userId, Long workId) {
        Bookshelf shelf = bookshelfRepository.findByUserIdAndWorkId(userId, workId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        bookshelfRepository.delete(shelf);
        return SuccessResponse.ok();
    }

    /**
     * 책장에 담을 Work를 찾거나 없으면 생성한다.
     * - API 책: ISBN이 있으면 ISBN 기준으로 공용 Work 재사용
     * - MANUAL 책: (등록자, 제목, 저자) 기준으로 사용자별 Work 재사용 (저자 null도 매칭)
     */
    private Work resolveWork(Long userId, AddBookshelfRequest request) {
        String isbn = normalizeIsbn(request.isbn());

        if (request.source() == BookSource.API && isbn != null) {
            return workRepository.findByIsbn(isbn)
                    .orElseGet(() -> createWorkHandlingIsbnRace(request, isbn, null));
        }

        if (request.source() == BookSource.MANUAL) {
            return workRepository.findManualWork(userId, request.title(), request.author())
                    .orElseGet(() -> createWork(request, isbn, userId));
        }

        // API인데 ISBN이 없는 경우 (공용, 재사용 판정 불가)
        return createWork(request, isbn, null);
    }

    /**
     * ISBN 유니크 제약 하에서 동시 추가 경합을 처리한다.
     * 저장이 유니크 위반으로 실패하면 다른 트랜잭션이 먼저 만든 Work를 재조회해 재사용한다.
     */
    private Work createWorkHandlingIsbnRace(AddBookshelfRequest request, String isbn, Long ownerUserId) {
        try {
            return createWork(request, isbn, ownerUserId);
        } catch (DataIntegrityViolationException e) {
            return workRepository.findByIsbn(isbn)
                    .orElseThrow(() -> e);
        }
    }

    private Work createWork(AddBookshelfRequest request, String isbn, Long ownerUserId) {
        return workRepository.save(Work.builder()
                .source(request.source())
                .title(request.title())
                .author(request.author())
                .publisher(request.publisher())
                .coverUrl(request.coverUrl())
                .isbn(isbn)
                .ownerUserId(ownerUserId)
                .build());
    }

    // 빈 문자열 ISBN은 null로 정규화한다. (빈 문자열 여러 건이 유니크 제약에 걸리지 않도록)
    private String normalizeIsbn(String isbn) {
        return StringUtils.hasText(isbn) ? isbn.trim() : null;
    }
}
