package com.nhnacademy.bookingservice.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("unused")
public class DailyBookingResponse {

    private Long no;

    private LocalDateTime date;

    private LocalDateTime finishedAt;

}
