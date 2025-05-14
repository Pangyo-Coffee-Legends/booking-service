package com.nhnacademy.bookingservice.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
@AllArgsConstructor
@SuppressWarnings("unused")
public class EmailRequest {

    private String to;

    private String subject;

    private String content;
}
