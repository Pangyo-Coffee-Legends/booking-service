package com.nhnacademy.bookingservice.service.impl;

import com.nhnacademy.bookingservice.common.adaptor.MeetingRoomAdaptor;
import com.nhnacademy.bookingservice.common.adaptor.MemberAdaptor;
import com.nhnacademy.bookingservice.common.exception.ForbiddenException;
import com.nhnacademy.bookingservice.common.exception.booking.AlreadyMeetingRoomTimeException;
import com.nhnacademy.bookingservice.common.exception.meeting.MeetingRoomNotFoundException;
import com.nhnacademy.bookingservice.common.exception.member.MemberNotFoundException;
import com.nhnacademy.bookingservice.dto.*;
import com.nhnacademy.bookingservice.entity.Booking;
import com.nhnacademy.bookingservice.entity.BookingChange;
import com.nhnacademy.bookingservice.repository.BookingChangeRepository;
import com.nhnacademy.bookingservice.repository.BookingRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
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
    private MemberAdaptor memberAdaptor;

    @Mock
    private MeetingRoomAdaptor meetingRoomAdaptor;

    @InjectMocks
    private BookingServiceImpl bookingService;

    MemberResponse memberInfo;
    MeetingRoomResponse meetingRoomResponse;
    BookingResponse bookingResponse;
    @BeforeEach
    void setUp() {
        memberInfo = new MemberResponse(1L, "test");
        meetingRoomResponse = new MeetingRoomResponse(1L, "회의실 A", 5);

        Booking booking = Booking.ofNewBooking("test", LocalDateTime.parse("2025-04-29T09:30:00"), 8, LocalDateTime.parse("2025-04-29T10:30:00"), 1L, null, 2L);
        ReflectionTestUtils.setField(booking, "no", 1L);

        bookingResponse = new BookingResponse(booking.getBookingNo(), booking.getBookingCode(), booking.getBookingDate(), booking.getAttendeeCount(), booking.getFinishedAt(), booking.getCreatedAt(), booking.getMbNo(),  null, booking.getRoomNo());

    }

    @Test
    @DisplayName("예약 생성")
    void register() {
        BookingRegisterRequest request = new BookingRegisterRequest(1L, "2025-04-29", "09:30", 8);

        when(bookingRepository.existsRoomNoAndDate(Mockito.anyLong(), Mockito.any())).thenReturn(false);

        bookingService.register(request);

        verify(bookingRepository, Mockito.times(1)).save(Mockito.any(Booking.class));
    }

    @Test
    @DisplayName("예약 생성 - 예약 중복")
    void register_exception_case() {
        BookingRegisterRequest request = new BookingRegisterRequest(1L, "2025-04-29", "09:30", 8);

        when(bookingRepository.existsRoomNoAndDate(Mockito.anyLong(), Mockito.any())).thenReturn(true);

        Assertions.assertThrows(AlreadyMeetingRoomTimeException.class, () -> {
            bookingService.register(request);
        });

        verify(bookingRepository, Mockito.never()).save(Mockito.any(Booking.class));
    }

    @Test
    @DisplayName("예약 조회")
    void getBooking() {
        when(bookingRepository.findByNo(Mockito.anyLong())).thenReturn(bookingResponse);
        when(meetingRoomAdaptor.getMeetingRoom(Mockito.anyLong())).thenReturn(ResponseEntity.ok(meetingRoomResponse));

        bookingService.getBooking(1L, memberInfo);

        verify(bookingRepository, Mockito.times(1)).findByNo(Mockito.anyLong());
        verify(meetingRoomAdaptor, Mockito.times(1)).getMeetingRoom(Mockito.anyLong());
    }

    @Test
    @DisplayName("예약 조회 - forbidden")
    void getBooking_exception_case1() {
         MemberResponse member = new MemberResponse(2L, "test2");

        when(bookingRepository.findByNo(Mockito.anyLong())).thenReturn(bookingResponse);

        Assertions.assertThrows(ForbiddenException.class, () -> {
            bookingService.getBooking(1L, member);
        });

        verify(bookingRepository, Mockito.times(1)).findByNo(Mockito.anyLong());
    }

    @Test
    @DisplayName("예약 조회 - not found(404)")
    void getBooking_exception_case2() {
        when(bookingRepository.findByNo(Mockito.anyLong())).thenReturn(bookingResponse);
        when(meetingRoomAdaptor.getMeetingRoom(Mockito.anyLong())).thenReturn(ResponseEntity.notFound().build());

        Assertions.assertThrows(MeetingRoomNotFoundException.class, () -> {
            bookingService.getBooking(1L, memberInfo);
        });

        verify(bookingRepository, Mockito.times(1)).findByNo(Mockito.anyLong());
        verify(meetingRoomAdaptor, Mockito.times(1)).getMeetingRoom(Mockito.anyLong());
    }

    @Test
    @DisplayName("예약 조회 - not found(null)")
    void getBooking_exception_case3() {
        when(bookingRepository.findByNo(Mockito.anyLong())).thenReturn(bookingResponse);
        when(meetingRoomAdaptor.getMeetingRoom(Mockito.anyLong())).thenReturn(ResponseEntity.ok().build());

        Assertions.assertThrows(MeetingRoomNotFoundException.class, () -> {
            bookingService.getBooking(1L, memberInfo);
        });

        verify(bookingRepository, Mockito.times(1)).findByNo(Mockito.anyLong());
        verify(meetingRoomAdaptor, Mockito.times(1)).getMeetingRoom(Mockito.anyLong());
    }

    @Test
    @DisplayName("예약 리스트 조회")
    void getBookings() {
        when(bookingRepository.findBookings(Pageable.ofSize(1))).thenReturn(new PageImpl<>(List.of(bookingResponse)));
        when(memberAdaptor.getMemberName(Mockito.anyLong())).thenReturn(ResponseEntity.ok(memberInfo));
        when(meetingRoomAdaptor.getMeetingRoom(Mockito.anyLong())).thenReturn(ResponseEntity.ok(meetingRoomResponse));

        bookingService.getBookings(Pageable.ofSize(1));

        verify(bookingRepository, Mockito.atLeast(1)).findBookings(Pageable.ofSize(1));
        verify(memberAdaptor, Mockito.atLeast(1)).getMemberName(Mockito.anyLong());
        verify(meetingRoomAdaptor, Mockito.atLeast(1)).getMeetingRoom(Mockito.anyLong());
    }

    @Test
    @DisplayName("예약 리스트 조회 - memberNotFound")
    void getBookings_exception_case1() {
        when(bookingRepository.findBookings(Pageable.ofSize(1))).thenReturn(new PageImpl<>(List.of(bookingResponse)));
        when(memberAdaptor.getMemberName(Mockito.anyLong())).thenReturn(ResponseEntity.notFound().build());

        Assertions.assertThrows(MemberNotFoundException.class, () -> {
            bookingService.getBookings(Pageable.ofSize(1));
        });

        verify(bookingRepository, Mockito.atLeast(1)).findBookings(Pageable.ofSize(1));
        verify(memberAdaptor, Mockito.atLeast(1)).getMemberName(Mockito.anyLong());
    }

    @Test
    @DisplayName("예약 리스트 조회 - meetingRoomNotFound")
    void getBookings_exception_case2() {
        when(bookingRepository.findBookings(Pageable.ofSize(1))).thenReturn(new PageImpl<>(List.of(bookingResponse)));
        when(memberAdaptor.getMemberName(Mockito.anyLong())).thenReturn(ResponseEntity.ok(memberInfo));
        when(meetingRoomAdaptor.getMeetingRoom(Mockito.anyLong())).thenReturn(ResponseEntity.notFound().build());

        Assertions.assertThrows(MeetingRoomNotFoundException.class, () -> {
            bookingService.getBookings(Pageable.ofSize(1));
        });

        verify(bookingRepository, Mockito.atLeast(1)).findBookings(Pageable.ofSize(1));
        verify(memberAdaptor, Mockito.atLeast(1)).getMemberName(Mockito.anyLong());
        verify(meetingRoomAdaptor, Mockito.atLeast(1)).getMeetingRoom(Mockito.anyLong());
    }

    @Test
    @DisplayName("예약 수정")
    void updateBooking() {
        BookingUpdateRequest request = new BookingUpdateRequest("2025-04-29", "11:30", 10);

        Booking booking = Booking.ofNewBooking("test", LocalDateTime.parse("2025-04-29T09:30:00"), 8, LocalDateTime.parse("2025-04-29T10:30:00"), 1L, null, 1L);
        ReflectionTestUtils.setField(booking, "no", 1L);

        BookingChange bookingChange = new BookingChange("변경");
        ReflectionTestUtils.setField(bookingChange, "no", 1L);

        when(bookingRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(booking));
        when(meetingRoomAdaptor.getMeetingRoom(Mockito.anyLong())).thenReturn(ResponseEntity.ok(meetingRoomResponse));
        when(bookingChangeRepository.findByName(Mockito.anyString())).thenReturn(bookingChange);

        bookingService.updateBooking(1L, request, memberInfo);

        verify(bookingRepository, Mockito.times(1)).findById(Mockito.anyLong());
        verify(meetingRoomAdaptor, Mockito.times(1)).getMeetingRoom(Mockito.anyLong());
        verify(bookingChangeRepository, Mockito.times(1)).findByName(Mockito.anyString());
    }

    @Test
    void updateBookingChange() {
        Booking booking = Booking.ofNewBooking("test", LocalDateTime.parse("2025-04-29T09:30:00"), 8, LocalDateTime.parse("2025-04-29T10:30:00"), 1L, null, 1L);
        ReflectionTestUtils.setField(booking, "no", 1L);

        BookingChange bookingChange = new BookingChange("연장");
        ReflectionTestUtils.setField(bookingChange, "no", 1L);

        when(bookingRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(booking));
        when(bookingChangeRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(bookingChange));

        bookingService.updateBookingChange(1L, 1L);

        verify(bookingRepository, Mockito.times(1)).findById(Mockito.anyLong());
        verify(bookingChangeRepository, Mockito.times(1)).findById(Mockito.anyLong());

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

        bookingService.cancelBooking(1L, memberInfo);

        verify(bookingRepository, Mockito.times(1)).findById(Mockito.anyLong());
        verify(bookingChangeRepository, Mockito.times(1)).findByName(Mockito.anyString());
    }
}