package com.galpi.galpibackend.domain.schedule.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.galpi.galpibackend.domain.quote.entity.Quote;
import com.galpi.galpibackend.domain.quote.entity.Visibility;
import com.galpi.galpibackend.domain.quote.repository.QuoteRepository;
import com.galpi.galpibackend.domain.schedule.dto.CreateScheduleRequest;
import com.galpi.galpibackend.domain.schedule.dto.ScheduleResponse;
import com.galpi.galpibackend.domain.schedule.entity.QuoteSchedule;
import com.galpi.galpibackend.domain.schedule.entity.RepeatType;
import com.galpi.galpibackend.domain.schedule.repository.QuoteScheduleRepository;
import com.galpi.galpibackend.domain.work.entity.BookSource;
import com.galpi.galpibackend.domain.work.entity.Work;
import com.galpi.galpibackend.global.error.CustomException;
import com.galpi.galpibackend.global.error.ErrorCode;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @Mock
    private QuoteScheduleRepository scheduleRepository;

    @Mock
    private QuoteRepository quoteRepository;

    @InjectMocks
    private ScheduleService scheduleService;

    private Quote quoteOwnedBy(long ownerUserId) {
        Work work = Work.builder().source(BookSource.API)
                .title("데미안").author("헤르만 헤세").build();
        ReflectionTestUtils.setField(work, "id", 10L);
        Quote quote = Quote.builder().userId(ownerUserId).work(work)
                .content("새는 알에서...").visibility(Visibility.PRIVATE).build();
        ReflectionTestUtils.setField(quote, "id", 100L);
        return quote;
    }

    @Test
    @DisplayName("본인 대사에 알림을 생성하면 스케줄을 저장한다")
    void createSchedule_success() {
        CreateScheduleRequest request = new CreateScheduleRequest(LocalTime.of(8, 0), RepeatType.DAILY, null);
        given(quoteRepository.findById(100L)).willReturn(Optional.of(quoteOwnedBy(1L)));
        given(scheduleRepository.save(any(QuoteSchedule.class))).willAnswer(invocation -> {
            QuoteSchedule saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 1L);
            return saved;
        });

        ScheduleResponse response = scheduleService.createSchedule(1L, 100L, request);

        assertThat(response.scheduleId()).isEqualTo(1L);
        assertThat(response.sendTime()).isEqualTo(LocalTime.of(8, 0));
        assertThat(response.repeatType()).isEqualTo(RepeatType.DAILY);
        assertThat(response.isActive()).isTrue();
    }

    @Test
    @DisplayName("남의 대사에 알림을 생성하려 하면 FORBIDDEN 예외를 던진다")
    void createSchedule_notOwner() {
        CreateScheduleRequest request = new CreateScheduleRequest(LocalTime.of(8, 0), RepeatType.DAILY, null);
        given(quoteRepository.findById(100L)).willReturn(Optional.of(quoteOwnedBy(2L)));

        assertThatThrownBy(() -> scheduleService.createSchedule(1L, 100L, request))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.FORBIDDEN);

        verify(scheduleRepository, never()).save(any());
    }

    @Test
    @DisplayName("WEEKLY 알림인데 daysOfWeek가 없으면 VALIDATION_ERROR 예외를 던진다")
    void createSchedule_weeklyWithoutDays() {
        CreateScheduleRequest request = new CreateScheduleRequest(LocalTime.of(8, 0), RepeatType.WEEKLY, null);
        given(quoteRepository.findById(100L)).willReturn(Optional.of(quoteOwnedBy(1L)));

        assertThatThrownBy(() -> scheduleService.createSchedule(1L, 100L, request))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    @Test
    @DisplayName("daysOfWeek에 잘못된 요일 토큰이 있으면 VALIDATION_ERROR 예외를 던진다")
    void createSchedule_invalidDaysOfWeek() {
        CreateScheduleRequest request = new CreateScheduleRequest(
                LocalTime.of(8, 0), RepeatType.WEEKLY, "MON,FUNDAY");
        given(quoteRepository.findById(100L)).willReturn(Optional.of(quoteOwnedBy(1L)));

        assertThatThrownBy(() -> scheduleService.createSchedule(1L, 100L, request))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    @Test
    @DisplayName("존재하지 않는 알림을 삭제하려 하면 NOT_FOUND 예외를 던진다")
    void deleteSchedule_notFound() {
        given(scheduleRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> scheduleService.deleteSchedule(1L, 99L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.NOT_FOUND);
    }

    @Test
    @DisplayName("남의 알림을 삭제하려 하면 FORBIDDEN 예외를 던진다")
    void deleteSchedule_notOwner() {
        QuoteSchedule schedule = QuoteSchedule.builder()
                .userId(2L).quote(quoteOwnedBy(2L)).sendTime(LocalTime.of(8, 0))
                .repeatType(RepeatType.DAILY).isActive(true).build();
        given(scheduleRepository.findById(1L)).willReturn(Optional.of(schedule));

        assertThatThrownBy(() -> scheduleService.deleteSchedule(1L, 1L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.FORBIDDEN);

        verify(scheduleRepository, never()).delete(any());
    }
}
