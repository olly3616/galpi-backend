package com.galpi.galpibackend.global.web;

import java.util.List;
import org.springframework.data.domain.Page;

/**
 * 모든 리스트 응답의 공통 형태. { items, page, hasNext }
 */
public record PageResponse<T>(
        List<T> items,
        int page,
        boolean hasNext
) {

    public static <T> PageResponse<T> of(List<T> items, int page, boolean hasNext) {
        return new PageResponse<>(items, page, hasNext);
    }

    /**
     * Spring Data Page에서 페이지 정보를 가져오고, 매핑된 items로 응답을 만든다.
     */
    public static <T> PageResponse<T> from(Page<?> source, List<T> items) {
        return new PageResponse<>(items, source.getNumber(), source.hasNext());
    }
}
