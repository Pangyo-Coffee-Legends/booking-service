package com.nhnacademy.bookingservice.common.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class BookingChangeEvent extends ApplicationEvent {
    private final String email;

    private final Long bookingNo;

    public BookingChangeEvent(Object source, String email, Long bookingNo) {
        super(source);
        this.email = email;
        this.bookingNo = bookingNo;
    }
}
