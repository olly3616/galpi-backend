package com.galpi.galpibackend.domain.quote.dto;

import com.galpi.galpibackend.domain.quote.entity.Quote;
import com.galpi.galpibackend.domain.quote.entity.Visibility;
import com.galpi.galpibackend.domain.work.dto.WorkBrief;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 내 전체 대사 목록(작품 무관)의 한 항목. 어느 책의 대사인지 표시/그룹핑할 수 있게 work를 포함한다.
 */
public record MyQuoteItem(
        @Schema(description = "대사 ID") Long quoteId,
        @Schema(description = "대사 본문") String content,
        @Schema(description = "등장인물(없으면 null)") String characterName,
        @Schema(description = "감상 메모(없으면 null)") String memo,
        @Schema(description = "이 대사에 설정된 알림이 하나라도 있는지") boolean hasSchedule,
        @Schema(description = "공개 범위") Visibility visibility,
        @Schema(description = "출처 책") WorkBrief work
) {

    public static MyQuoteItem from(Quote quote, boolean hasSchedule) {
        return new MyQuoteItem(
                quote.getId(),
                quote.getContent(),
                quote.getCharacterName(),
                quote.getMemo(),
                hasSchedule,
                quote.getVisibility(),
                WorkBrief.from(quote.getWork())
        );
    }
}
