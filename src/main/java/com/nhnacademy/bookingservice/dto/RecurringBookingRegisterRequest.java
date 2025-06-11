package com.nhnacademy.bookingservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class RecurringBookingRegisterRequest {

    private String title;

    @DateTimeFormat(pattern = "hh:mm")
    @NotNull(message = "시작 시간은 비어 있을 수 없습니다.")
    private LocalTime startTime;

    @NotNull(message = "요일은 비어 있을 수 없습니다.")
    private Set<DayOfWeek> dayOfWeek;

    @NotNull(message = "인원 수는 비어 있을 수 없습니다.")
    private Integer attendeeCount;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "시작날짜는 비어 있을 수 없습니다.")
    private LocalDate startDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "끝나는 날짜는 비어 있을 수 없습니다.")
    private LocalDate endDate;

    private Long roomNo;
}
