package com.nhnacademy.bookingservice.common.exception;

public class ConflictException extends CommonHttpException{
    private static final int STATUS_CODE = 409;

    public ConflictException(){
        super("Conflict with existing resource", STATUS_CODE);
    }

    public ConflictException(String message) {
        super(message, STATUS_CODE);
    }
}
