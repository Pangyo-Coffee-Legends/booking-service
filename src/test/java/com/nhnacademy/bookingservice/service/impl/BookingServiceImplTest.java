package com.nhnacademy.bookingservice.service.impl;

import com.nhnacademy.bookingservice.common.adaptor.MeetingRoomAdaptor;
import com.nhnacademy.bookingservice.entity.Booking;
import com.nhnacademy.bookingservice.entity.BookingChange;
import com.nhnacademy.bookingservice.dto.BookingRegisterRequest;
import com.nhnacademy.bookingservice.dto.BookingUpdateRequest;
import com.nhnacademy.bookingservice.dto.MeetingRoomResponse;
import com.nhnacademy.bookingservice.dto.MemberInfoResponse;
import com.nhnacademy.bookingservice.repository.BookingChangeRepository;
import com.nhnacademy.bookingservice.repository.BookingRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BookingChangeRepository bookingChangeRepository;

    @Mock
    private MeetingRoomAdaptor meetingRoomAdaptor;

    @InjectMocks
    private BookingServiceImpl bookingService;

    @Test
    @DisplayName("예약 생성")
    void save() {
        BookingRegisterRequest request = new BookingRegisterRequest(1L, "2025-04-29", "09:30", 8);

        bookingService.save(request);

        verify(bookingRepository, Mockito.times(1)).save(Mockito.any(Booking.class));
    }

    @Test
    @DisplayName("예약 조회")
    void getBooking() {
        MemberInfoResponse memberInfo = new MemberInfoResponse(1L, "test", "test@test.com", "010-1111-2222");
        MeetingRoomResponse meetingRoomResponse = new MeetingRoomResponse(1L, "회의실 A");

        Booking booking = Booking.ofNewBooking("test", LocalDateTime.parse("2025-04-29T09:30:00"), 8, LocalDateTime.parse("2025-04-29T10:30:00"), 1L, null, 1L);
        ReflectionTestUtils.setField(booking, "no", 1L);

        when(bookingRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(booking));
        when(meetingRoomAdaptor.getMeetingRoom(Mockito.anyLong())).thenReturn(ResponseEntity.ok(meetingRoomResponse));

        bookingService.getBooking(1L, memberInfo);

        verify(bookingRepository, Mockito.times(1)).findById(Mockito.anyLong());
        verify(meetingRoomAdaptor, Mockito.times(1)).getMeetingRoom(Mockito.anyLong());
    }

    // todo 코드 변경
    @Test
    @DisplayName("예약 수정")
    void updateBooking() {
        BookingUpdateRequest request = new BookingUpdateRequest("2025-04-29", "11:30", 10);

        Booking booking = Booking.ofNewBooking("test", LocalDateTime.parse("2025-04-29T09:30:00"), 8, LocalDateTime.parse("2025-04-29T10:30:00"), 1L, null, 1L);
        ReflectionTestUtils.setField(booking, "no", 1L);

        BookingChange bookingChange = new BookingChange("변경");
        ReflectionTestUtils.setField(bookingChange, "no", 1L);

        when(bookingRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(booking));
        when(bookingChangeRepository.findByName(Mockito.anyString())).thenReturn(bookingChange);

        bookingService.updateBooking(1L, request);

        verify(bookingRepository, Mockito.times(1)).findById(Mockito.anyLong());
        verify(bookingChangeRepository, Mockito.times(1)).findByName(Mockito.anyString());
    }

    @Test
    @DisplayName("예약 취소")
    void cancelBooking() {
        Booking booking = Booking.ofNewBooking("test", LocalDateTime.parse("2025-04-29T09:30:00"), 8, LocalDateTime.parse("2025-04-29T10:30:00"), 1L, null, 1L);
        ReflectionTestUtils.setField(booking, "no", 1L);

        BookingChange bookingChange = new BookingChange("취소");
        ReflectionTestUtils.setField(bookingChange, "no", 1L);

        when(bookingRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(booking));
        when(bookingChangeRepository.findByName(Mockito.anyString())).thenReturn(bookingChange);

        bookingService.cancelBooking(1L);

        verify(bookingRepository, Mockito.times(1)).findById(Mockito.anyLong());
        verify(bookingChangeRepository, Mockito.times(1)).findByName(Mockito.anyString());
    }
}