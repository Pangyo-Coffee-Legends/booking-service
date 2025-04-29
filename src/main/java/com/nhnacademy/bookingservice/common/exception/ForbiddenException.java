package com.nhnacademy.bookingservice.common.exception;

public class ForbiddenException extends CommonHttpException{
    private static final int STATUS_CODE = 403;

    public ForbiddenException() {
        super("잘못된 접근 입니다.", STATUS_CODE);
    }

    public ForbiddenException(String message) {
        super(message, STATUS_CODE);
    }
}
