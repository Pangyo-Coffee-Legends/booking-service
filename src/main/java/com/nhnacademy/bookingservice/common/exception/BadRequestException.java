package com.nhnacademy.bookingservice.common.exception;

public class BadRequestException extends CommonHttpException{
    private static final int STATUS_CODE = 400;

    public BadRequestException() {
        super("잘못된 요청입니다.", STATUS_CODE);
    }

    public BadRequestException(String message) {
        super(message, STATUS_CODE);
    }
}
