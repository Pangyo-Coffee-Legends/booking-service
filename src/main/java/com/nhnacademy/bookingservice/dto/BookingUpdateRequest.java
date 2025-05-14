package com.nhnacademy.bookingservice.dto;

import lombok.*;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class BookingUpdateRequest {

    String date;

    String time;

    Integer attendeeCount;

    Long roomNo;
}
