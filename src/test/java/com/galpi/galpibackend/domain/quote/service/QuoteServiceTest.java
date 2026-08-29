package com.galpi.galpibackend.domain.quote.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.galpi.galpibackend.domain.quote.dto.CreateQuoteRequest;
import com.galpi.galpibackend.domain.quote.dto.QuoteResponse;
import com.galpi.galpibackend.domain.quote.dto.UpdateQuoteRequest;
import com.galpi.galpibackend.domain.quote.dto.WorkQuotesResponse;
import com.galpi.galpibackend.domain.quote.entity.Quote;
import com.galpi.galpibackend.domain.quote.entity.Visibility;
import com.galpi.galpibackend.domain.like.repository.LikeRepository;
import com.galpi.galpibackend.domain.quote.repository.QuoteRepository;
import com.galpi.galpibackend.domain.schedule.repository.QuoteScheduleRepository;
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
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class QuoteServiceTest {

    @Mock
    private QuoteRepository quoteRepository;

    @Mock
    private WorkRepository workRepository;

    @Mock
    private QuoteScheduleRepository scheduleRepository;

    @Mock
    private LikeRepository likeRepository;

    @InjectMocks
    private QuoteService quoteService;

    private Work workWithId(long id) {
        Work work = Work.builder()
                .source(BookSource.API)
                .type(BookType.NOVEL)
                .title("데미안")
                .author("헤르만 헤세")
                .build();
        ReflectionTestUtils.setField(work, "id", id);
        return work;
    }

    private Quote quoteWithId(long id, long ownerUserId) {
        Quote quote = Quote.builder()
                .userId(ownerUserId)
                .work(workWithId(10L))
                .content("새는 알에서 나오려고 투쟁한다.")
                .visibility(Visibility.PRIVATE)
                .build();
        ReflectionTestUtils.setField(quote, "id", id);
        return quote;
    }

    @Test
    @DisplayName("대사 작성 시 visibility 미지정이면 PRIVATE로 저장한다")
    void createQuote_defaultsPrivate() {
        CreateQuoteRequest request = new CreateQuoteRequest(10L, "데미안",
                "새는 알에서 나오려고 투쟁한다.", "인생 문장", null);
        given(workRepository.findById(10L)).willReturn(Optional.of(workWithId(10L)));
        given(quoteRepository.save(any(Quote.class))).willAnswer(invocation -> {
            Quote saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 100L);
            return saved;
        });

        QuoteResponse response = quoteService.createQuote(1L, request);

        assertThat(response.quoteId()).isEqualTo(100L);
        assertThat(response.visibility()).isEqualTo(Visibility.PRIVATE);
        assertThat(response.work().workId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("존재하지 않는 책에 대사를 작성하면 NOT_FOUND 예외를 던진다")
    void createQuote_workNotFound() {
        CreateQuoteRequest request = new CreateQuoteRequest(99L, null, "본문", null, null);
        given(workRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> quoteService.createQuote(1L, request))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.NOT_FOUND);
    }

    @Test
    @DisplayName("본인 대사는 상세 조회할 수 있다")
    void getQuote_ownSuccess() {
        given(quoteRepository.findById(100L)).willReturn(Optional.of(quoteWithId(100L, 1L)));
        given(scheduleRepository.findByQuoteIdOrderByCreatedAtAsc(100L)).willReturn(List.of());

        QuoteResponse response = quoteService.getQuote(1L, 100L);

        assertThat(response.quoteId()).isEqualTo(100L);
        assertThat(response.schedules()).isEmpty();
    }

    @Test
    @DisplayName("남의 대사를 상세 조회하면 FORBIDDEN 예외를 던진다")
    void getQuote_notOwnerForbidden() {
        given(quoteRepository.findById(100L)).willReturn(Optional.of(quoteWithId(100L, 2L)));

        assertThatThrownBy(() -> quoteService.getQuote(1L, 100L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.FORBIDDEN);
    }

    @Test
    @DisplayName("대사 수정 시 전달된 필드만 변경한다")
    void updateQuote_partial() {
        Quote quote = quoteWithId(100L, 1L);
        given(quoteRepository.findById(100L)).willReturn(Optional.of(quote));
        UpdateQuoteRequest request = new UpdateQuoteRequest("수정된 본문", null, null, Visibility.FOLLOWERS);

        QuoteResponse response = quoteService.updateQuote(1L, 100L, request);

        assertThat(response.content()).isEqualTo("수정된 본문");
        assertThat(response.visibility()).isEqualTo(Visibility.FOLLOWERS);
    }

    @Test
    @DisplayName("남의 대사를 수정하려 하면 FORBIDDEN 예외를 던진다")
    void updateQuote_notOwnerForbidden() {
        given(quoteRepository.findById(100L)).willReturn(Optional.of(quoteWithId(100L, 2L)));
        UpdateQuoteRequest request = new UpdateQuoteRequest("해킹", null, null, null);

        assertThatThrownBy(() -> quoteService.updateQuote(1L, 100L, request))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.FORBIDDEN);
    }

    @Test
    @DisplayName("본인 대사를 삭제하면 deleted=true를 반환한다")
    void deleteQuote_success() {
        Quote quote = quoteWithId(100L, 1L);
        given(quoteRepository.findById(100L)).willReturn(Optional.of(quote));

        var response = quoteService.deleteQuote(1L, 100L);

        assertThat(response.success()).isTrue();
        verify(scheduleRepository).deleteByQuoteId(100L);
        verify(likeRepository).deleteByQuoteId(100L);
        verify(quoteRepository).delete(quote);
    }

    @Test
    @DisplayName("남의 대사를 삭제하려 하면 FORBIDDEN 예외를 던지고 삭제하지 않는다")
    void deleteQuote_notOwnerForbidden() {
        Quote quote = quoteWithId(100L, 2L);
        given(quoteRepository.findById(100L)).willReturn(Optional.of(quote));

        assertThatThrownBy(() -> quoteService.deleteQuote(1L, 100L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.FORBIDDEN);

        verify(quoteRepository, never()).delete(any(Quote.class));
    }

    @Test
    @DisplayName("책의 내 대사 모아보기는 출처(work)와 대사 목록을 반환한다")
    void getWorkQuotes_success() {
        given(workRepository.findById(10L)).willReturn(Optional.of(workWithId(10L)));
        given(quoteRepository.findByUserIdAndWorkIdOrderByCreatedAtDesc(eq(1L), eq(10L), any()))
                .willReturn(new PageImpl<>(List.of(quoteWithId(100L, 1L))));
        given(scheduleRepository.findQuoteIdsWithScheduleIn(List.of(100L))).willReturn(List.of());

        WorkQuotesResponse response = quoteService.getWorkQuotes(1L, 10L, 0, 20);

        assertThat(response.work().workId()).isEqualTo(10L);
        assertThat(response.quotes().items()).hasSize(1);
        assertThat(response.quotes().items().get(0).quoteId()).isEqualTo(100L);
        assertThat(response.quotes().items().get(0).hasSchedule()).isFalse();
    }
}
