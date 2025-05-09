package com.nhnacademy.bookingservice.common.exception.booking;

public class BookingTimeNotReachedException extends RuntimeException {
    public BookingTimeNotReachedException() {
        super("예약 시간 10분 전부터 입장 가능합니다.");
    }
}
