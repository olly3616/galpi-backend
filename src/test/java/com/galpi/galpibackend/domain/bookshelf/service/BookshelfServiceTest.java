package com.galpi.galpibackend.domain.bookshelf.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.galpi.galpibackend.domain.bookshelf.dto.AddBookshelfRequest;
import com.galpi.galpibackend.domain.bookshelf.dto.AddBookshelfResponse;
import com.galpi.galpibackend.domain.bookshelf.dto.BookshelfResponse;
import com.galpi.galpibackend.domain.bookshelf.entity.Bookshelf;
import com.galpi.galpibackend.domain.bookshelf.repository.BookshelfRepository;
import com.galpi.galpibackend.domain.work.entity.BookSource;
import com.galpi.galpibackend.domain.work.entity.BookType;
import com.galpi.galpibackend.domain.work.entity.Work;
import com.galpi.galpibackend.domain.work.repository.WorkRepository;
import com.galpi.galpibackend.global.error.CustomException;
import com.galpi.galpibackend.global.error.ErrorCode;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class BookshelfServiceTest {

    @Mock
    private BookshelfRepository bookshelfRepository;

    @Mock
    private WorkRepository workRepository;

    @InjectMocks
    private BookshelfService bookshelfService;

    private Work workWithId(long id) {
        Work work = Work.builder()
                .source(BookSource.API)
                .type(BookType.NOVEL)
                .title("데미안")
                .author("헤르만 헤세")
                .isbn("9788937460449")
                .build();
        ReflectionTestUtils.setField(work, "id", id);
        return work;
    }

    @Test
    @DisplayName("API 책 추가 시 ISBN이 이미 있으면 기존 Work를 재사용하고 책장에 저장한다")
    void addBook_apiExistingWork() {
        AddBookshelfRequest request = new AddBookshelfRequest(
                BookSource.API, "데미안", "헤르만 헤세", "민음사",
                "https://cover", "9788937460449", BookType.NOVEL);
        Work existing = workWithId(10L);
        given(workRepository.findByIsbn("9788937460449")).willReturn(Optional.of(existing));
        given(bookshelfRepository.existsByUserIdAndWorkId(1L, 10L)).willReturn(false);

        AddBookshelfResponse response = bookshelfService.addBook(1L, request);

        assertThat(response.workId()).isEqualTo(10L);
        assertThat(response.addedToShelf()).isTrue();
        verify(workRepository, never()).save(any(Work.class));
        verify(bookshelfRepository).save(any(Bookshelf.class));
    }

    @Test
    @DisplayName("MANUAL 책 추가 시 기존 Work가 없으면 새로 생성한다")
    void addBook_manualNewWork() {
        AddBookshelfRequest request = new AddBookshelfRequest(
                BookSource.MANUAL, "전지적 독자 시점", "싱숑", null,
                null, null, BookType.WEBNOVEL);
        given(workRepository.findByOwnerUserIdAndTitleAndAuthor(1L, "전지적 독자 시점", "싱숑"))
                .willReturn(Optional.empty());
        given(workRepository.save(any(Work.class))).willAnswer(invocation -> {
            Work saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 20L);
            return saved;
        });
        given(bookshelfRepository.existsByUserIdAndWorkId(1L, 20L)).willReturn(false);

        AddBookshelfResponse response = bookshelfService.addBook(1L, request);

        assertThat(response.workId()).isEqualTo(20L);
        verify(workRepository).save(any(Work.class));
        verify(bookshelfRepository).save(any(Bookshelf.class));
    }

    @Test
    @DisplayName("이미 책장에 있는 책이면 ALREADY_IN_SHELF 예외를 던진다")
    void addBook_alreadyInShelf() {
        AddBookshelfRequest request = new AddBookshelfRequest(
                BookSource.API, "데미안", "헤르만 헤세", "민음사",
                "https://cover", "9788937460449", BookType.NOVEL);
        Work existing = workWithId(10L);
        given(workRepository.findByIsbn("9788937460449")).willReturn(Optional.of(existing));
        given(bookshelfRepository.existsByUserIdAndWorkId(1L, 10L)).willReturn(true);

        assertThatThrownBy(() -> bookshelfService.addBook(1L, request))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.ALREADY_IN_SHELF);

        verify(bookshelfRepository, never()).save(any(Bookshelf.class));
    }

    @Test
    @DisplayName("내 책장 조회 시 Work 정보를 items로 매핑한다")
    void getMyBookshelf_mapsItems() {
        Work work = workWithId(10L);
        Bookshelf shelf = Bookshelf.builder().userId(1L).work(work).build();
        given(bookshelfRepository.findByUserIdOrderByCreatedAtDesc(eq(1L), any()))
                .willReturn(new PageImpl<>(List.of(shelf), PageRequest.of(0, 20), 1));

        BookshelfResponse response = bookshelfService.getMyBookshelf(1L, 0, 20);

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).workId()).isEqualTo(10L);
        assertThat(response.items().get(0).title()).isEqualTo("데미안");
        assertThat(response.hasNext()).isFalse();
    }

    @Test
    @DisplayName("책장에 없는 책을 빼려 하면 NOT_FOUND 예외를 던진다")
    void removeBook_notFound() {
        given(bookshelfRepository.findByUserIdAndWorkId(1L, 99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> bookshelfService.removeBook(1L, 99L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.NOT_FOUND);
    }

    @Test
    @DisplayName("책장에서 책을 빼면 삭제 후 removed=true를 반환한다")
    void removeBook_success() {
        Bookshelf shelf = Bookshelf.builder().userId(1L).work(workWithId(10L)).build();
        given(bookshelfRepository.findByUserIdAndWorkId(1L, 10L)).willReturn(Optional.of(shelf));

        var response = bookshelfService.removeBook(1L, 10L);

        assertThat(response.removed()).isTrue();
        verify(bookshelfRepository).delete(shelf);
    }
}
