package com.nhnacademy.bookingservice.dto;

import lombok.*;

@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode
@SuppressWarnings("unused")
public class MemberResponse {
    private Long no;

    private String name;

    private String email;

    private String phoneNumber;

    private String roleName;
}
