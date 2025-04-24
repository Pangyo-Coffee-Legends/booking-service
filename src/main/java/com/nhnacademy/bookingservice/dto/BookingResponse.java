package com.nhnacademy.bookingservice.dto;

import lombok.Value;

import java.time.LocalDateTime;

@Value
public class BookingResponse {

    Long no;

    String code;

    LocalDateTime date;

    Integer attendees;

    LocalDateTime finishedAt;

    LocalDateTime createdAt;

    String mbName;

    String changeName;

    String roomName;

}
