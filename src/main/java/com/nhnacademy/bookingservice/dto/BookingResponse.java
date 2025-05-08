package com.nhnacademy.bookingservice.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class BookingResponse {

    private Long no;

    private String code;

    @EqualsAndHashCode.Exclude
    private LocalDateTime date;

    private Integer attendeeCount;

    @EqualsAndHashCode.Exclude
    private LocalDateTime finishedAt;

    private Long mbNo;

    @Setter
    private String mbName;

    @Setter
    private String email;

    private String changeName;

    private Long roomNo;

    @Setter
    private String roomName;

    @QueryProjection
    public BookingResponse(Long no, String code, LocalDateTime date, Integer attendeeCount, LocalDateTime finishedAt, Long mbNo, String changeName, Long roomNo) {
        this.no = no;
        this.code = code;
        this.date = date;
        this.attendeeCount = attendeeCount;
        this.finishedAt = finishedAt;
        this.mbNo = mbNo;
        this.changeName = changeName;
        this.roomNo = roomNo;
    }
}
