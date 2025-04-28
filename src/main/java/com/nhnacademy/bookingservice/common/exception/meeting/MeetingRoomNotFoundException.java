package com.nhnacademy.bookingservice.common.exception.meeting;

import com.nhnacademy.bookingservice.common.exception.NotFoundException;

@SuppressWarnings("java:S110")
public class MeetingRoomNotFoundException extends NotFoundException {

    public MeetingRoomNotFoundException(){
        super("회의실 정보를 찾을 수 없습니다.");
    }

    public MeetingRoomNotFoundException(Long no) {
        super("회의실 정보를 찾을 수 없습니다. (id : %d)".formatted(no));
    }

    public MeetingRoomNotFoundException(String name) {
        super("회의실 정보를 찾을 수 없습니다. %s".formatted(name));
    }
}
