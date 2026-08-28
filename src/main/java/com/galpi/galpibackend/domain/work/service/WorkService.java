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
    public WorkResponse getWork(Long workId) {
        Work work = workRepository.findById(workId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
        return WorkResponse.from(work);
    }
}
