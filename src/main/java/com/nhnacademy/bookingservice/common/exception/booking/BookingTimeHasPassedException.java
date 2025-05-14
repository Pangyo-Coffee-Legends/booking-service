package com.nhnacademy.bookingservice.common.exception.booking;

import com.nhnacademy.bookingservice.common.exception.BadRequestException;

@SuppressWarnings("java:S110")
public class BookingTimeHasPassedException extends BadRequestException {
    public BookingTimeHasPassedException() {
        super("예약시간 10분 후까지만 입실 가능합니다.");
    }
}
