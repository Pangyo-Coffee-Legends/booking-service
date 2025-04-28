package com.nhnacademy.bookingservice.common.exception.booking;

import com.nhnacademy.bookingservice.common.exception.NotFoundException;

@SuppressWarnings("java:S110")
public class BookingChangeNotFoundException extends NotFoundException {

    public BookingChangeNotFoundException(){
        super("해당 변경 내용을 찾을 수 없습니다.");
    }

    public BookingChangeNotFoundException(Long no){
        super("해당 변경 내용을 찾을 수 없습니다. [id: %s]".formatted(no));
    }

    public BookingChangeNotFoundException(String name){
        super("해당 변경 내용을 찾을 수 없습니다. [name: %s]".formatted(name));
    }
}
