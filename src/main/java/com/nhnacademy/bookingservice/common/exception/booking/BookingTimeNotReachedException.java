package com.nhnacademy.bookingservice.common.exception.booking;

import com.nhnacademy.bookingservice.common.exception.BadRequestException;

@SuppressWarnings("java:S110")
public class BookingTimeNotReachedException extends BadRequestException {
    public BookingTimeNotReachedException() {
        super("예약 시간 10분 전부터 입장 가능합니다.");
    }
}
