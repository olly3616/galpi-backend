package com.galpi.galpibackend.domain.quote.service;

import com.galpi.galpibackend.domain.quote.dto.CreateQuoteRequest;
import com.galpi.galpibackend.domain.quote.dto.DeleteQuoteResponse;
import com.galpi.galpibackend.domain.quote.dto.QuoteResponse;
import com.galpi.galpibackend.domain.quote.dto.UpdateQuoteRequest;
import com.galpi.galpibackend.domain.quote.dto.WorkQuotesResponse;
import com.galpi.galpibackend.domain.quote.dto.WorkQuotesResponse.QuoteSummary;
import com.galpi.galpibackend.domain.quote.entity.Quote;
import com.galpi.galpibackend.domain.quote.entity.Visibility;
import com.galpi.galpibackend.domain.quote.repository.QuoteRepository;
import com.galpi.galpibackend.domain.schedule.dto.ScheduleResponse;
import com.galpi.galpibackend.domain.schedule.repository.QuoteScheduleRepository;
import com.galpi.galpibackend.domain.work.dto.WorkBrief;
import com.galpi.galpibackend.domain.work.entity.Work;
import com.galpi.galpibackend.domain.work.repository.WorkRepository;
import com.galpi.galpibackend.global.error.CustomException;
import com.galpi.galpibackend.global.error.ErrorCode;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuoteService {

    private final QuoteRepository quoteRepository;
    private final WorkRepository workRepository;
    private final QuoteScheduleRepository scheduleRepository;

    public QuoteService(QuoteRepository quoteRepository, WorkRepository workRepository,
                        QuoteScheduleRepository scheduleRepository) {
        this.quoteRepository = quoteRepository;
        this.workRepository = workRepository;
        this.scheduleRepository = scheduleRepository;
    }

    @Transactional
    public QuoteResponse createQuote(Long userId, CreateQuoteRequest request) {
        Work work = workRepository.findById(request.workId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        Visibility visibility = request.visibility() != null ? request.visibility() : Visibility.PRIVATE;

        Quote quote = Quote.builder()
                .userId(userId)
                .work(work)
                .characterName(request.characterName())
                .content(request.content())
                .memo(request.memo())
                .visibility(visibility)
                .build();
        quoteRepository.save(quote);

        return QuoteResponse.from(quote);
    }

    @Transactional(readOnly = true)
    public QuoteResponse getQuote(Long userId, Long quoteId) {
        Quote quote = findQuote(quoteId);
        // 현재는 본인 대사만 조회 가능. (팔로워 공개 접근은 F-13에서 구현)
        if (!quote.isOwnedBy(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
        List<ScheduleResponse> schedules = scheduleRepository
                .findByQuoteIdOrderByCreatedAtAsc(quoteId).stream()
                .map(ScheduleResponse::from)
                .toList();
        return QuoteResponse.of(quote, schedules);
    }

    @Transactional(readOnly = true)
    public WorkQuotesResponse getWorkQuotes(Long userId, Long workId) {
        Work work = workRepository.findById(workId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        List<Quote> quoteList = quoteRepository.findByUserIdAndWorkIdOrderByCreatedAtDesc(userId, workId);
        List<Long> quoteIds = quoteList.stream().map(Quote::getId).toList();
        Set<Long> scheduledQuoteIds = quoteIds.isEmpty()
                ? Set.of()
                : Set.copyOf(scheduleRepository.findQuoteIdsWithScheduleIn(quoteIds));

        List<QuoteSummary> quotes = quoteList.stream()
                .map(quote -> QuoteSummary.from(quote, scheduledQuoteIds.contains(quote.getId())))
                .toList();

        return new WorkQuotesResponse(WorkBrief.from(work), quotes);
    }

    @Transactional
    public QuoteResponse updateQuote(Long userId, Long quoteId, UpdateQuoteRequest request) {
        Quote quote = findQuote(quoteId);
        if (!quote.isOwnedBy(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        if (request.content() != null) {
            quote.changeContent(request.content());
        }
        if (request.memo() != null) {
            quote.changeMemo(request.memo());
        }
        if (request.characterName() != null) {
            quote.changeCharacterName(request.characterName());
        }
        if (request.visibility() != null) {
            quote.changeVisibility(request.visibility());
        }

        return QuoteResponse.from(quote);
    }

    @Transactional
    public DeleteQuoteResponse deleteQuote(Long userId, Long quoteId) {
        Quote quote = findQuote(quoteId);
        if (!quote.isOwnedBy(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
        // 연결된 알림(quote_schedules)도 함께 삭제
        scheduleRepository.deleteByQuoteId(quoteId);
        quoteRepository.delete(quote);
        return new DeleteQuoteResponse(true);
    }

    private Quote findQuote(Long quoteId) {
        return quoteRepository.findById(quoteId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
    }
}
