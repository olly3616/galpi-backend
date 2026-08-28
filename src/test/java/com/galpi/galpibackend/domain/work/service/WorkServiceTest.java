package com.galpi.galpibackend.domain.work.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.galpi.galpibackend.domain.work.dto.WorkResponse;
import com.galpi.galpibackend.domain.work.entity.BookSource;
import com.galpi.galpibackend.domain.work.entity.BookType;
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
    @DisplayName("책 상세 조회 성공 시 Work 정보를 반환한다")
    void getWork_success() {
        Work work = Work.builder()
                .source(BookSource.API)
                .type(BookType.NOVEL)
                .title("데미안")
                .author("헤르만 헤세")
                .build();
        ReflectionTestUtils.setField(work, "id", 10L);
        given(workRepository.findById(10L)).willReturn(Optional.of(work));

        WorkResponse response = workService.getWork(10L);

        assertThat(response.workId()).isEqualTo(10L);
        assertThat(response.title()).isEqualTo("데미안");
        assertThat(response.source()).isEqualTo(BookSource.API);
    }

    @Test
    @DisplayName("존재하지 않는 책을 조회하면 NOT_FOUND 예외를 던진다")
    void getWork_notFound() {
        given(workRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> workService.getWork(99L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.NOT_FOUND);
    }
}
