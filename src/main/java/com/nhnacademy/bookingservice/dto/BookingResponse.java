package com.nhnacademy.bookingservice.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@SuppressWarnings("java:S107")
public class BookingResponse {

    private Long no;

    private String code;

    @EqualsAndHashCode.Exclude
    private LocalDateTime startsAt;

    private Integer attendeeCount;

    @EqualsAndHashCode.Exclude
    private LocalDateTime finishesAt;

    @EqualsAndHashCode.Exclude
    private LocalDateTime createdAt;

    private String changeName;

    private MemberInfo member;

    private MeetingRoomInfo room;

    @Getter
    @Setter
    public static class MemberInfo {
        private Long no;

        private String name;

        private String email;
    }

    @Getter
    @Setter
    public static class MeetingRoomInfo {
        private Long no;

        private String name;
    }

    @QueryProjection
    public BookingResponse(Long no, String code, LocalDateTime startsAt, Integer attendeeCount, LocalDateTime finishesAt, LocalDateTime createdAt, String changeName, Long mbNo, Long roomNo) {
        this.no = no;
        this.code = code;
        this.startsAt = startsAt;
        this.attendeeCount = attendeeCount;
        this.finishesAt = finishesAt;
        this.createdAt = createdAt;
        this.changeName = changeName;

        this.member = new MemberInfo();
        this.member.setNo(mbNo);

        this.room = new MeetingRoomInfo();
        this.room.setNo(roomNo);
    }
}