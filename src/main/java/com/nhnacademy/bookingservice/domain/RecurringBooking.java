package com.nhnacademy.bookingservice.domain;

import com.nhnacademy.bookingservice.domain.convert.DayOfWeekConvert;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Entity
@Table(name = "recurring_bookings")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecurringBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("recurring_booking_no")
    private Long recurringBookingNo;

    @Column(name = "recurring_booking_title", length = 100, nullable = true)
    @Comment("정기예약 제목")
    private String recurringBookingTitle;

    @Column(name = "start_time", nullable = false)
    @Comment("정기예약 시작 시간")
    private LocalTime startTime;

    @Convert(converter = DayOfWeekConvert.class)
    @Column(name = "day_of_week", columnDefinition = "tinyint", nullable = false)
    @Comment("정기예약 요일")
    private DayOfWeek dayOfWeek;

    @Column(name = "start_date", nullable = false)
    @Comment("시작 날짜")
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    @Comment("끝나는 날짜")
    private LocalDate endDate;

    @Column(name = "created_at", nullable = false)
    @Comment("생성일시")
    private LocalDateTime createdAt;

    @Column(name = "mb_no", nullable = false)
    @Comment("예약자 번호")
    private Long mbNo;

    @Column(name = "meeting_room_no", nullable = false)
    @Comment("회의실 번호")
    private Long meetingRoomNo;

    @PrePersist
    private void prePersist(){
        this.createdAt = LocalDateTime.now();
    }

    private RecurringBooking(String recurringBookingTitle, LocalTime startTime, DayOfWeek dayOfWeek, LocalDate startDate, LocalDate endDate, Long mbNo, Long meetingRoomNo) {
        this.recurringBookingTitle = recurringBookingTitle;
        this.startTime = startTime;
        this.dayOfWeek = dayOfWeek;
        this.startDate = startDate;
        this.endDate = endDate;
        this.mbNo = mbNo;
        this.meetingRoomNo = meetingRoomNo;
    }

    public static RecurringBooking ofNewRecurringBooking(String recurringBookingTitle, LocalTime startTime, DayOfWeek dayOfWeek, LocalDate startDate, LocalDate endDate, Long mbNo, Long meetingRoomNo) {
        return new RecurringBooking(
                recurringBookingTitle,
                startTime,
                dayOfWeek,
                startDate,
                endDate,
                mbNo,
                meetingRoomNo
        );
    }

    public void update(String recurringBookingTitle, LocalTime startTime, DayOfWeek dayOfWeek, LocalDate startDate, LocalDate endDate, Long mbNo, Long meetingRoomNo){
        this.recurringBookingTitle = recurringBookingTitle;
        this.startTime = startTime;
        this.dayOfWeek = dayOfWeek;
        this.startDate = startDate;
        this.endDate = endDate;
        this.mbNo = mbNo;
        this.meetingRoomNo = meetingRoomNo;
    }
}
