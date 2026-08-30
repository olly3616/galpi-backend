package com.galpi.galpibackend.domain.work.service;

import com.galpi.galpibackend.domain.work.dto.WorkResponse;
import com.galpi.galpibackend.domain.work.entity.Work;
import com.galpi.galpibackend.domain.work.repository.WorkRepository;
import com.galpi.galpibackend.global.error.CustomException;
import com.galpi.galpibackend.global.error.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkService {

    private final WorkRepository workRepository;

    public WorkService(WorkRepository workRepository) {
        this.workRepository = workRepository;
    }

    @Transactional(readOnly = true)
    public WorkResponse getWork(Long userId, Long workId) {
        Work work = workRepository.findById(workId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
        // API 책(ownerUserId=null)은 공용 카탈로그라 누구나 조회 가능하지만,
        // 직접 등록한(MANUAL) 책은 등록한 본인만 조회할 수 있다.
        // 남의 MANUAL 책은 존재 자체를 숨기기 위해 FORBIDDEN 대신 NOT_FOUND로 응답한다.
        if (work.getOwnerUserId() != null && !work.getOwnerUserId().equals(userId)) {
            throw new CustomException(ErrorCode.NOT_FOUND);
        }
        return WorkResponse.from(work);
    }
}
