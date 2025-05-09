package com.nhnacademy.bookingservice.common.exception.booking;

public class BookingInfoDoesNotMatchException extends RuntimeException {

    public BookingInfoDoesNotMatchException() {
        super("예약정보가 일치하지 않습니다.");
    }
}
