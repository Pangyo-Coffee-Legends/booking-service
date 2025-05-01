package com.nhnacademy.bookingservice.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_no")
    private Long bookingNo;

    @Column(name = "booking_code", length = 10, nullable = false, unique = true)
    @Comment("예약코드")
    private String bookingCode;

    @Column(name = "booking_date", nullable = false)
    @Comment("에약일시")
    private LocalDateTime bookingDate;

    @Column(name = "attendee_count", nullable = false)
    @Comment("예약인원")
    private Integer attendeeCount;

    @Column(name = "finished_at", nullable = true)
    @Comment("회의종료시간")
    private LocalDateTime finishedAt;

    @Column(name = "created_at", nullable = false)
    @Comment("생성시간")
    private LocalDateTime createdAt;

    @Column(name = "mb_no", nullable = false)
    @Comment("예약자번호")
    private Long mbNo;

    @Column(name = "room_no", nullable = false)
    @Comment("회의실번호")
    private Long roomNo;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "changes_no", referencedColumnName = "changes_no", nullable = true)
    @Comment("특이사항번호")
    private BookingChange bookingChange;

   @PrePersist
   void prePersist(){
       this.createdAt = LocalDateTime.now();
   }

    private Booking(String bookingCode, LocalDateTime bookingDate, Integer attendeeCount, LocalDateTime finishedAt, Long mbNo, BookingChange bookingChange, Long roomNo) {
        this.bookingCode = bookingCode;
        this.bookingDate = bookingDate;
        this.attendeeCount = attendeeCount;
        this.finishedAt = finishedAt;
        this.mbNo = mbNo;
        this.bookingChange = bookingChange;
        this.roomNo = roomNo;
    }

    public static Booking ofNewBooking(String code, LocalDateTime date, Integer attendees, LocalDateTime finishedAt, Long mbNo, BookingChange bookingChange, Long roomNo){
       return new Booking(code, date, attendees, finishedAt, mbNo, bookingChange, roomNo);
    }

    public void update(LocalDateTime date, Integer attendees, LocalDateTime finishedAt){
       this.bookingDate = date;
       this.attendeeCount = attendees;
       this.finishedAt = finishedAt;
    }

    public void updateBookingEvent(BookingChange bookingChange){
       this.bookingChange = bookingChange;
    }

    public void updateFinishedAt(LocalDateTime finishedAt){
       this.finishedAt = finishedAt;
    }
}
