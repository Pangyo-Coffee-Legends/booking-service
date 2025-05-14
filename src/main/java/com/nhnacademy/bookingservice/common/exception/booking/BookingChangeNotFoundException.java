package com.nhnacademy.bookingservice.common.exception.booking;

import com.nhnacademy.bookingservice.common.exception.NotFoundException;

@SuppressWarnings("java:S110")
public class BookingChangeNotFoundException extends NotFoundException {

    public BookingChangeNotFoundException(Long no){
        super("해당 특이사항(%s)을 찾을 수 없습니다.".formatted(no));
    }
}
