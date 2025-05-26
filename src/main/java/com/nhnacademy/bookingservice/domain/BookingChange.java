package com.nhnacademy.bookingservice.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "booking_changes")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString
public class BookingChange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "change_no")
    private Long no;

    @Column(name = "change_name", length = 10, nullable = false)
    @Comment("특이사항")
    private String name;

    public BookingChange(String name) {
        this.name = name;
    }

    public void updateName(String name) {
        this.name = name;
    }

}
