package com.nhnacademy.bookingservice.dto;

import lombok.*;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class BookingUpdateRequest {

    String date;

    String startTime;

    String finishTime;

    Integer attendeeCount;

    Long roomNo;
}
