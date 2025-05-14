package com.nhnacademy.bookingservice.common.exception.meeting;

import com.nhnacademy.bookingservice.common.exception.BadRequestException;

@SuppressWarnings("java:S110")
public class MeetingRoomCapacityExceededException extends BadRequestException {
    public MeetingRoomCapacityExceededException(int capacity) {
        super("예약 인원이 회의실 최대 수용 인원(%d명)을 초과하였습니다.".formatted(capacity));
    }
}
