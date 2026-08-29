package com.galpi.galpibackend.global.web;

/**
 * 별도 상태를 반환하지 않는 확인용(mutation) 응답의 공통 형태. { success: true }
 * (삭제/제거/등록확인/로그아웃 등)
 */
public record SuccessResponse(
        boolean success
) {

    public static SuccessResponse ok() {
        return new SuccessResponse(true);
    }
}
