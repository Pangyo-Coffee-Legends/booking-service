package com.nhnacademy.bookingservice.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {

    private Long no;

    private String code;

    private LocalDateTime date;

    private Integer attendees;

    private LocalDateTime finishedAt;

    private LocalDateTime createdAt;

    private Long mbNo;

    @Setter
    private String mbName;

    private String changeName;

    private Long roomNo;

    @Setter
    private String roomName;

}
