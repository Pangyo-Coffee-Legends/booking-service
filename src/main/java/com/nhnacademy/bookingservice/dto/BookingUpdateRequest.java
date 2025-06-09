package com.nhnacademy.bookingservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class BookingUpdateRequest {

    @NotNull(message = "회의실 번호는 필수입니다.")
    private Long roomNo;

    @NotBlank(message = "예약 날짜는 비어 있을 수 없습니다.")
    private String date;

    @NotBlank(message = "시작 시간은 비어 있을 수 없습니다.")
    private String startTime;

    private String finishTime;

    @NotNull(message = "참석 인원 수는 필수입니다.")
    private Integer attendeeCount;
}
