package com.nhnacademy.bookingservice.common.exception.booking;

import com.nhnacademy.bookingservice.common.exception.BadRequestException;

public class InvalidBookingTimeException extends BadRequestException {
    public InvalidBookingTimeException() {
        super("과거 시간으로는 예약할 수 없습니다.");
    }
}
