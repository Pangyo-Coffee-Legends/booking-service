package com.nhnacademy.bookingservice.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@SuppressWarnings("unused")
public class DailyBookingResponse {

    private Long no;

    private Long mbNo;

    @Setter
    private String mbName;

    private Integer attendeeCount;

    private LocalDateTime startsAt;

    private LocalDateTime finishesAt;

    private String bookingChangeName;

    @QueryProjection
    public DailyBookingResponse(Long no, Long mbNo, Integer attendeeCount, LocalDateTime startsAt, LocalDateTime finishesAt, String bookingChangeName) {
        this.no = no;
        this.mbNo = mbNo;
        this.attendeeCount = attendeeCount;
        this.startsAt = startsAt;
        this.finishesAt = finishesAt;
        this.bookingChangeName = bookingChangeName;
    }

}
