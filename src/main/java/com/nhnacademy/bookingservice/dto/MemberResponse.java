package com.nhnacademy.bookingservice.dto;

import lombok.*;

@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode
public class MemberResponse {
    private Long no;

    private String name;

    private String email;

    private String phoneNumber;

    private String roleName;
}
