package com.galpi.galpibackend.domain.bookshelf.service;

import com.galpi.galpibackend.domain.bookshelf.dto.AddBookshelfRequest;
import com.galpi.galpibackend.domain.bookshelf.dto.AddBookshelfResponse;
import com.galpi.galpibackend.domain.bookshelf.dto.BookshelfResponse;
import com.galpi.galpibackend.domain.bookshelf.dto.BookshelfResponse.BookshelfItem;
import com.galpi.galpibackend.domain.bookshelf.dto.RemoveBookshelfResponse;
import com.galpi.galpibackend.domain.bookshelf.entity.Bookshelf;
import com.galpi.galpibackend.domain.bookshelf.repository.BookshelfRepository;
import com.galpi.galpibackend.domain.work.entity.BookSource;
import com.galpi.galpibackend.domain.work.entity.Work;
import com.galpi.galpibackend.domain.work.repository.WorkRepository;
import com.galpi.galpibackend.global.error.CustomException;
import com.galpi.galpibackend.global.error.ErrorCode;
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

    public BookshelfService(BookshelfRepository bookshelfRepository, WorkRepository workRepository) {
        this.bookshelfRepository = bookshelfRepository;
        this.workRepository = workRepository;
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

        return new AddBookshelfResponse(work.getId(), true);
    }

    @Transactional(readOnly = true)
    public BookshelfResponse getMyBookshelf(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Bookshelf> shelfPage = bookshelfRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        var items = shelfPage.getContent().stream()
                .map(shelf -> {
                    Work work = shelf.getWork();
                    // TODO(F-08): 대사 기능 구현 후 실제 대사 개수로 대체
                    long quoteCount = 0L;
                    return new BookshelfItem(work.getId(), work.getTitle(), work.getAuthor(),
                            work.getCoverUrl(), quoteCount);
                })
                .toList();

        return new BookshelfResponse(items, page, shelfPage.hasNext());
    }

    @Transactional
    public RemoveBookshelfResponse removeBook(Long userId, Long workId) {
        Bookshelf shelf = bookshelfRepository.findByUserIdAndWorkId(userId, workId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        bookshelfRepository.delete(shelf);
        return new RemoveBookshelfResponse(true);
    }

    /**
     * 책장에 담을 Work를 찾거나 없으면 생성한다.
     * - API 책: ISBN이 있으면 ISBN 기준으로 공용 Work 재사용
     * - MANUAL 책: (등록자, 제목, 저자) 기준으로 사용자별 Work 재사용
     */
    private Work resolveWork(Long userId, AddBookshelfRequest request) {
        if (request.source() == BookSource.API && StringUtils.hasText(request.isbn())) {
            return workRepository.findByIsbn(request.isbn())
                    .orElseGet(() -> createWork(request, null));
        }

        if (request.source() == BookSource.MANUAL) {
            return workRepository.findByOwnerUserIdAndTitleAndAuthor(userId, request.title(), request.author())
                    .orElseGet(() -> createWork(request, userId));
        }

        // API인데 ISBN이 없는 경우
        return createWork(request, null);
    }

    private Work createWork(AddBookshelfRequest request, Long ownerUserId) {
        return workRepository.save(Work.builder()
                .source(request.source())
                .type(request.type())
                .title(request.title())
                .author(request.author())
                .publisher(request.publisher())
                .coverUrl(request.coverUrl())
                .isbn(request.isbn())
                .ownerUserId(ownerUserId)
                .build());
    }
}
