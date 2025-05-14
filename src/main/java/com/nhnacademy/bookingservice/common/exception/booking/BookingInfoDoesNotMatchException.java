package com.nhnacademy.bookingservice.common.exception.booking;

import com.nhnacademy.bookingservice.common.exception.BadRequestException;

@SuppressWarnings("java:S110")
public class BookingInfoDoesNotMatchException extends BadRequestException {

    public BookingInfoDoesNotMatchException() {
        super("예약정보가 일치하지 않습니다.");
    }
}
