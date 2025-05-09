package com.nhnacademy.bookingservice.common.exception.booking;

import com.nhnacademy.bookingservice.common.exception.ConflictException;

import java.time.LocalDateTime;

@SuppressWarnings("java:S110")
public class AlreadyMeetingRoomTimeException extends ConflictException {
    public AlreadyMeetingRoomTimeException(){
        super("선택하신 시간에 회의실이 이미 예약되어 있습니다. 다른 시간대를 선택해 주세요.");
    }

    public AlreadyMeetingRoomTimeException(LocalDateTime dateTime){
        super("선택하신 시간에 회의실이 이미 예약되어 있습니다. 다른 시간대를 선택해 주세요.[시간: %s".formatted(dateTime));
    }
}
