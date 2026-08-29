package com.galpi.galpibackend.domain.work.dto;

import com.galpi.galpibackend.domain.work.entity.Work;

/**
 * 공유 대사에 붙는 출처 표기(작품 제목·작가). 저작권상 피드/프로필 응답에 필수 포함된다.
 */
public record WorkSource(
        String title,
        String author
) {

    public static WorkSource from(Work work) {
        return new WorkSource(work.getTitle(), work.getAuthor());
    }
}
