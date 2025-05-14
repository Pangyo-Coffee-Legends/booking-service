package com.nhnacademy.bookingservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class EntryResponse {

    private String code;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime entryTime;

    private Long meetingRoomNo;

}
