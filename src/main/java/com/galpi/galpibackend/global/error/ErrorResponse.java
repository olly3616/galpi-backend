package com.galpi.galpibackend.global.error;

public record ErrorResponse(ErrorDetail error) {

    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(new ErrorDetail(errorCode.name(), errorCode.getMessage()));
    }

    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return new ErrorResponse(new ErrorDetail(errorCode.name(), message));
    }

    public record ErrorDetail(String code, String message) {
    }
}
