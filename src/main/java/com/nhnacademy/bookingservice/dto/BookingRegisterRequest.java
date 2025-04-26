package com.nhnacademy.bookingservice.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;


@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode
public class BookingRegisterRequest {

    private Long roomNo;

    private String date;

    private String time;

    private Integer attendeeCount;
}
