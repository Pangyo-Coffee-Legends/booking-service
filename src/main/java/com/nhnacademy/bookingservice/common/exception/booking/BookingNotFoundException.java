package com.nhnacademy.bookingservice.common.exception.booking;

import com.nhnacademy.bookingservice.common.exception.NotFoundException;

@SuppressWarnings("java:S110")
public class BookingNotFoundException extends NotFoundException {

    public BookingNotFoundException() {
        super("예약을 찾을 수 없습니다.");
    }

    public BookingNotFoundException(Long no) {
        super("예약을 찾을 수 없습니다(id: %d)".formatted(no));
    }

    public BookingNotFoundException(String name) {
        super("예약을 찾을 수 없습니다. %s".formatted(name));
    }
}
