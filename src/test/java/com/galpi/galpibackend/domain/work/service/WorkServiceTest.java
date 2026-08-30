package com.galpi.galpibackend.domain.work.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.galpi.galpibackend.domain.work.dto.WorkResponse;
import com.galpi.galpibackend.domain.work.entity.BookSource;
import com.galpi.galpibackend.domain.work.entity.Work;
import com.galpi.galpibackend.domain.work.repository.WorkRepository;
import com.galpi.galpibackend.global.error.CustomException;
import com.galpi.galpibackend.global.error.ErrorCode;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class WorkServiceTest {

    @Mock
    private WorkRepository workRepository;

    @InjectMocks
    private WorkService workService;

    @Test
    @DisplayName("API 책(공용)은 소유자가 아니어도 조회할 수 있다")
    void getWork_apiBook_anyUser() {
        Work work = Work.builder()
                .source(BookSource.API)
                .title("데미안")
                .author("헤르만 헤세")
                .build(); // ownerUserId = null (공용)
        ReflectionTestUtils.setField(work, "id", 10L);
        given(workRepository.findById(10L)).willReturn(Optional.of(work));

        WorkResponse response = workService.getWork(999L, 10L);

        assertThat(response.workId()).isEqualTo(10L);
        assertThat(response.title()).isEqualTo("데미안");
        assertThat(response.source()).isEqualTo(BookSource.API);
    }

    @Test
    @DisplayName("직접 등록한(MANUAL) 책은 등록한 본인이 조회할 수 있다")
    void getWork_manualBook_owner() {
        Work work = Work.builder()
                .source(BookSource.MANUAL)
                .title("나의 노트")
                .ownerUserId(1L)
                .build();
        ReflectionTestUtils.setField(work, "id", 20L);
        given(workRepository.findById(20L)).willReturn(Optional.of(work));

        WorkResponse response = workService.getWork(1L, 20L);

        assertThat(response.workId()).isEqualTo(20L);
        assertThat(response.title()).isEqualTo("나의 노트");
    }

    @Test
    @DisplayName("남이 직접 등록한(MANUAL) 책을 조회하면 NOT_FOUND 예외를 던진다")
    void getWork_manualBook_notOwner() {
        Work work = Work.builder()
                .source(BookSource.MANUAL)
                .title("남의 노트")
                .ownerUserId(2L)
                .build();
        ReflectionTestUtils.setField(work, "id", 30L);
        given(workRepository.findById(30L)).willReturn(Optional.of(work));

        assertThatThrownBy(() -> workService.getWork(1L, 30L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.NOT_FOUND);
    }

    @Test
    @DisplayName("존재하지 않는 책을 조회하면 NOT_FOUND 예외를 던진다")
    void getWork_notFound() {
        given(workRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> workService.getWork(1L, 99L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.NOT_FOUND);
    }
}
