package com.nhnacademy.bookingservice.dto;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.Value;

/**
 * 회원 정보를 조회하는 DTO 클래스입니다.
 * <p>
 * 이 클래스는 클라이언트가 회원의 정보를 조회할 때 응답 형식으로 사용되며,
 * 회원의 역할, 이름, 이메일, 전화번호 등의 정보를 포함합니다.
 * </p>
 */

@Value
public class MemberInfoResponse {

    Long no;

    String name;

    String email;

    String phoneNumber;
}
