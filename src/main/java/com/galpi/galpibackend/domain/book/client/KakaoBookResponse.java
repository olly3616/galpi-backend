package com.galpi.galpibackend.domain.book.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * 카카오 도서 검색 API 원본 응답 구조.
 * https://developers.kakao.com/docs/latest/ko/daum-search/dev-guide#search-book
 */
public record KakaoBookResponse(
        Meta meta,
        List<Document> documents
) {

    public record Meta(
            @JsonProperty("is_end") boolean isEnd,
            @JsonProperty("pageable_count") int pageableCount,
            @JsonProperty("total_count") int totalCount
    ) {
    }

    public record Document(
            String title,
            List<String> authors,
            String publisher,
            String thumbnail,
            String isbn
    ) {
    }
}
