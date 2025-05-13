package com.nhnacademy.bookingservice.common.event;

import com.nhnacademy.bookingservice.common.adaptor.MeetingRoomAdaptor;
import com.nhnacademy.bookingservice.common.adaptor.NotifyAdaptor;
import com.nhnacademy.bookingservice.dto.BookingResponse;
import com.nhnacademy.bookingservice.dto.EmailRequest;
import com.nhnacademy.bookingservice.dto.MeetingRoomResponse;
import com.nhnacademy.bookingservice.repository.BookingRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BookingEmailEventListenerTest {

    @Mock
    private MeetingRoomAdaptor meetingRoomAdaptor;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private NotifyAdaptor notifyAdaptor;

    @InjectMocks
    private BookingEmailEventListener listener;


    @Test
    @DisplayName("이메일 발송 - 예약 생성")
    void handleBookingCreatedEvent() {
        BookingResponse bookingResponse = new BookingResponse(1L, "test", LocalDateTime.parse("2025-04-29T09:30:00"), 8, LocalDateTime.parse("2025-04-29T08:30:00"), LocalDateTime.parse("2025-04-29T09:30:00"), 1L, "test", null,null, 1L, "회의실 A");
        MeetingRoomResponse roomResponse = new MeetingRoomResponse(1L, "회의실 A", 6);

        String email = "test@example.com";
        Long bookingNo = 123L;
        BookingCreatedEvent event = new BookingCreatedEvent(this, email, bookingNo);

        when(bookingRepository.findByNo(Mockito.anyLong())).thenReturn(Optional.of(bookingResponse));
        when(meetingRoomAdaptor.getMeetingRoom(Mockito.anyLong())).thenReturn(roomResponse);

        listener.handleBookingCreatedEvent(event);

        verify(notifyAdaptor, Mockito.times(1)).sendHtmlEmail(Mockito.any(EmailRequest.class));
    }

    @Test
    @DisplayName("이메일 발송 - 예약 취소")
    void handleBookingCancelEvent() {
        BookingResponse bookingResponse = new BookingResponse(1L, "test", LocalDateTime.parse("2025-04-29T09:30:00"), 8, LocalDateTime.parse("2025-04-29T08:30:00"), LocalDateTime.parse("2025-04-29T09:30:00"), 1L, "test", null,null, 1L, "회의실 A");
        MeetingRoomResponse roomResponse = new MeetingRoomResponse(1L, "회의실 A", 6);

        String email = "test@example.com";
        Long bookingNo = 123L;
        BookingCancelEvent event = new BookingCancelEvent(this, email, bookingNo);

        when(bookingRepository.findByNo(Mockito.anyLong())).thenReturn(Optional.of(bookingResponse));
        when(meetingRoomAdaptor.getMeetingRoom(Mockito.anyLong())).thenReturn(roomResponse);

        listener.handleBookingCancelEvent(event);

        verify(notifyAdaptor, Mockito.times(1)).sendHtmlEmail(Mockito.any(EmailRequest.class));

    }
}