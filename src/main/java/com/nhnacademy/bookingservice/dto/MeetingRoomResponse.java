package com.nhnacademy.bookingservice.dto;

import lombok.*;

@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode
public class MeetingRoomResponse {

    private final Long no;

    private final String meetingRoomName;

    private final int meetingRoomCapacity;
}
