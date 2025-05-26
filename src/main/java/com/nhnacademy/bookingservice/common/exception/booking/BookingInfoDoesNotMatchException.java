package com.nhnacademy.bookingservice.common.exception.booking;

import com.nhnacademy.bookingservice.common.exception.NotFoundException;

@SuppressWarnings("java:S110")
public class BookingInfoDoesNotMatchException extends NotFoundException {

    public BookingInfoDoesNotMatchException() {
        super("예약정보가 일치하지 않습니다.");
    }
}
