package com.nhnacademy.bookingservice.common.exception;

public class CommonHttpException extends RuntimeException{
    private final int statusCode;

    public CommonHttpException(final String  message, final int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public CommonHttpException(final String message, final Throwable cause, final int statusCode) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
