package com.nhnacademy.bookingservice.common.exception.member;

import com.nhnacademy.bookingservice.common.exception.NotFoundException;

@SuppressWarnings("java:S110")
public class MemberNotFoundException extends NotFoundException {
    public MemberNotFoundException() {
        super("회원 정보를 찾을 수 없습니다.");
    }

    public MemberNotFoundException(Long no) {
        super("회원 정보를 찾을 수 없습니다.[id: %s]".formatted(no));
    }
}
