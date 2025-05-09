package com.nhnacademy.bookingservice.common.exception.booking;

public class BookingTimeHasPassedException extends RuntimeException {
    public BookingTimeHasPassedException() {
        super("예약시간 10분 후까지만 입실 가능합니다.");
    }
}
