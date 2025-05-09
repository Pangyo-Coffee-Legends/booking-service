package com.nhnacademy.bookingservice.common.exception;

public class NotFoundException extends CommonHttpException {
    private static final int STATUS_CODE = 404;


    public NotFoundException() {
        super("resource not found", STATUS_CODE);
    }

    public NotFoundException(String message) {
        super(message, STATUS_CODE);
    }

}
