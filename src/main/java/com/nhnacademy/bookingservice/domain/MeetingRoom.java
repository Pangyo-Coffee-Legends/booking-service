package com.nhnacademy.bookingservice.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "meeting_rooms")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString
public class MeetingRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "meeting_room_no")
    private Long no;

    @Column(name = "meeting_room_name", length = 10, nullable = false)
    @Comment("회의실이름")
    private String name;

    @Column(name = "meeting_room_capacity", nullable = false)
    @Comment("수용인원")
    private Integer capacity;

    private MeetingRoom(String name, Integer capacity) {
        this.name = name;
        this.capacity = capacity;
    }

    public static MeetingRoom ofNewMeetingRoom(String name, Integer capacity) {
        return new MeetingRoom(name, capacity);
    }

    public void update(String name, Integer capacity){
        this.name = name;
        this.capacity = capacity;
    }
}
