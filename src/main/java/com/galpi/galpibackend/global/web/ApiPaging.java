package com.galpi.galpibackend.global.web;

/**
 * 페이지네이션 파라미터 기본값. (@RequestParam defaultValue에서 상수로 재사용)
 */
public final class ApiPaging {

    public static final String DEFAULT_PAGE = "0";
    public static final String DEFAULT_SIZE = "20";

    private ApiPaging() {
    }
}
