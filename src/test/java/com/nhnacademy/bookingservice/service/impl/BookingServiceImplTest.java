package com.nhnacademy.bookingservice.service.impl;

import com.nhnacademy.bookingservice.common.adaptor.MeetingRoomAdaptor;
import com.nhnacademy.bookingservice.common.adaptor.MemberAdaptor;
import com.nhnacademy.bookingservice.common.event.BookingCancelEvent;
import com.nhnacademy.bookingservice.common.event.BookingCreatedEvent;
import com.nhnacademy.bookingservice.common.exception.BadRequestException;
import com.nhnacademy.bookingservice.common.exception.ForbiddenException;
import com.nhnacademy.bookingservice.common.exception.booking.AlreadyMeetingRoomTimeException;
import com.nhnacademy.bookingservice.common.exception.booking.BookingNotFoundException;
import com.nhnacademy.bookingservice.common.exception.meeting.MeetingRoomCapacityExceededException;
import com.nhnacademy.bookingservice.common.exception.meeting.MeetingRoomNotFoundException;
import com.nhnacademy.bookingservice.common.exception.member.MemberNotFoundException;
import com.nhnacademy.bookingservice.dto.*;
import com.nhnacademy.bookingservice.entity.Booking;
import com.nhnacademy.bookingservice.entity.BookingChange;
import com.nhnacademy.bookingservice.entity.BookingChangeType;
import com.nhnacademy.bookingservice.repository.BookingChangeRepository;
import com.nhnacademy.bookingservice.repository.BookingRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private ApplicationEventPublisher publisher;

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
        memberInfo = new MemberResponse(1L, "test", "test@test.com", "010-1111-1111", "ROLE_USER");
        meetingRoomResponse = new MeetingRoomResponse(1L, "회의실 A", 5);

        Booking booking = Booking.ofNewBooking("test", LocalDateTime.parse("2025-04-29T09:30:00"), 8, LocalDateTime.parse("2025-04-29T10:30:00"), 1L, null, 2L);
        ReflectionTestUtils.setField(booking, "bookingNo", 1L);

        bookingResponse = new BookingResponse(booking.getBookingNo(), booking.getBookingCode(), booking.getBookingDate(), booking.getAttendeeCount(), booking.getFinishedAt(), booking.getCreatedAt(), booking.getMbNo(),  null, booking.getRoomNo());

    }

    @Test
    @DisplayName("예약 생성 성공")
    void register_case1() {
        BookingRegisterRequest request = new BookingRegisterRequest(1L, "2025-04-29", "09:30", 6);
        MeetingRoomResponse roomResponse = new MeetingRoomResponse(1L, "회의실 A", 6);
        when(meetingRoomAdaptor.getMeetingRoom(Mockito.anyLong())).thenReturn(roomResponse);
        when(bookingRepository.existsRoomNoAndDate(Mockito.anyLong(), Mockito.any())).thenReturn(false);

        bookingService.register(request, memberInfo);

        Mockito.verify(bookingRepository, Mockito.times(1)).save(Mockito.any(Booking.class));
        Mockito.verify(publisher, Mockito.times(1)).publishEvent(Mockito.any(BookingCreatedEvent.class));

    }

    @Test
    @DisplayName("예약 생성 실패 - 수용인원 초과")
    void register_exception_case1() {
        BookingRegisterRequest request = new BookingRegisterRequest(1L, "2025-04-29", "09:30", 8);
        MeetingRoomResponse roomResponse = new MeetingRoomResponse(1L, "회의실 A", 6);

        when(meetingRoomAdaptor.getMeetingRoom(Mockito.anyLong())).thenReturn(roomResponse);
        Assertions.assertThrows(MeetingRoomCapacityExceededException.class, () -> bookingService.register(request, memberInfo));

        Mockito.verify(bookingRepository, Mockito.never()).save(Mockito.any(Booking.class));
    }

    @Test
    @DisplayName("예약 생성 실패 - 예약 중복")
    void register_exception_case2() {
        BookingRegisterRequest request = new BookingRegisterRequest(1L, "2025-04-29", "09:30", 5);
        MeetingRoomResponse roomResponse = new MeetingRoomResponse(1L, "회의실 A", 6);

        when(meetingRoomAdaptor.getMeetingRoom(Mockito.anyLong())).thenReturn(roomResponse);
        when(bookingRepository.existsRoomNoAndDate(Mockito.anyLong(), Mockito.any())).thenReturn(true);

        Assertions.assertThrows(AlreadyMeetingRoomTimeException.class, () -> bookingService.register(request, memberInfo));

        Mockito.verify(bookingRepository, Mockito.never()).save(Mockito.any(Booking.class));
    }

    @Test
    @DisplayName("예약 생성 실패 - 예약 중복")
    void register_exception_case3() {
        BookingRegisterRequest request = new BookingRegisterRequest(1L, "2025-04-29", "10:00", 5);
        MeetingRoomResponse roomResponse = new MeetingRoomResponse(1L, "회의실 A", 6);

        when(meetingRoomAdaptor.getMeetingRoom(Mockito.anyLong())).thenReturn(roomResponse);
        when(bookingRepository.existsRoomNoAndDate(Mockito.anyLong(), Mockito.any())).thenReturn(true);

        Assertions.assertThrows(AlreadyMeetingRoomTimeException.class, () -> bookingService.register(request, memberInfo));

        Mockito.verify(bookingRepository, Mockito.never()).save(Mockito.any(Booking.class));
    }

    @Test
    @DisplayName("예약 조회")
    void getBooking() {
        when(bookingRepository.findByNo(Mockito.anyLong())).thenReturn(Optional.of(bookingResponse));
        when(meetingRoomAdaptor.getMeetingRoom(Mockito.anyLong())).thenReturn(meetingRoomResponse);

        bookingService.getBooking(1L, memberInfo);

        Mockito.verify(bookingRepository, Mockito.times(1)).findByNo(Mockito.anyLong());
        Mockito.verify(meetingRoomAdaptor, Mockito.times(1)).getMeetingRoom(Mockito.anyLong());
    }

    @Test
    @DisplayName("예약 조회 - forbidden")
    void getBooking_exception_case1() {
         MemberResponse member = new MemberResponse(2L, "test2", "test2@test.com", "010-1111-1111","ROLE_USER");

        when(bookingRepository.findByNo(Mockito.anyLong())).thenReturn(Optional.of(bookingResponse));

        Assertions.assertThrows(ForbiddenException.class, () -> bookingService.getBooking(1L, member));

        Mockito.verify(bookingRepository, Mockito.times(1)).findByNo(Mockito.anyLong());
    }

    @Test
    @DisplayName("예약 조회 - booking not found")
    void getBooking_exception_case2() {
        MemberResponse member = new MemberResponse(2L, "test2", "test2@test.com", "010-1111-1111", "ROLE_USER");

        when(bookingRepository.findByNo(Mockito.anyLong())).thenReturn(Optional.empty());

        Assertions.assertThrows(BookingNotFoundException.class, () -> bookingService.getBooking(1L, member));

        Mockito.verify(bookingRepository, Mockito.times(1)).findByNo(Mockito.anyLong());
    }

    @Test
    @DisplayName("예약 조회 - meeting not found")
    void getBooking_exception_case3() {
        when(bookingRepository.findByNo(Mockito.anyLong())).thenReturn(Optional.of(bookingResponse));
        when(meetingRoomAdaptor.getMeetingRoom(Mockito.anyLong())).thenThrow(MeetingRoomNotFoundException.class);

        Assertions.assertThrows(MeetingRoomNotFoundException.class, () -> bookingService.getBooking(1L, memberInfo));

        Mockito.verify(bookingRepository, Mockito.times(1)).findByNo(Mockito.anyLong());
        Mockito.verify(meetingRoomAdaptor, Mockito.times(1)).getMeetingRoom(Mockito.anyLong());
    }
//
//    @Test
//    @DisplayName("예약 조회 - meeting not found(null)")
//    void getBooking_exception_case4() {
//        when(bookingRepository.findByNo(Mockito.anyLong())).thenReturn(Optional.of(bookingResponse));
//        when(meetingRoomAdaptor.getMeetingRoom(Mockito.anyLong())).thenReturn(ResponseEntity.ok().build());
//
//        Assertions.assertThrows(MeetingRoomNotFoundException.class, () -> bookingService.getBooking(1L, memberInfo));
//
//        verify(bookingRepository, Mockito.times(1)).findByNo(Mockito.anyLong());
//        verify(meetingRoomAdaptor, Mockito.times(1)).getMeetingRoom(Mockito.anyLong());
//    }

    @Test
    @DisplayName("예약 사용자별 조회 - 리스트")
    void getBookingsByMember_list() {
        when(bookingRepository.findBookingList(1L)).thenReturn(List.of(bookingResponse));
        when(meetingRoomAdaptor.getMeetingRoom(Mockito.anyLong())).thenReturn(meetingRoomResponse);

        bookingService.getBookingsByMember(memberInfo);

        Mockito.verify(bookingRepository, Mockito.atLeast(1)).findBookingList(1L);
        Mockito.verify(meetingRoomAdaptor, Mockito.atLeast(1)).getMeetingRoom(Mockito.anyLong());
    }

    @Test
    @DisplayName("예약 전체 조회 - 리스트")
    void getAllBookings_list() {
        when(bookingRepository.findBookingList(null)).thenReturn(List.of(bookingResponse));
        when(memberAdaptor.getMember(Mockito.anyLong())).thenReturn(memberInfo);
        when(meetingRoomAdaptor.getMeetingRoom(Mockito.anyLong())).thenReturn(meetingRoomResponse);

        bookingService.getAllBookings();

        Mockito.verify(bookingRepository, Mockito.atLeast(1)).findBookingList(null);
        Mockito.verify(memberAdaptor, Mockito.atLeast(1)).getMember(Mockito.anyLong());
        Mockito.verify(meetingRoomAdaptor, Mockito.atLeast(1)).getMeetingRoom(Mockito.anyLong());
    }

    @Test
    @DisplayName("예약 사용자별 조회 - 페이징")
    void getBookingsByMember_page() {
        when(bookingRepository.findBookings(1L, Pageable.ofSize(1))).thenReturn(new PageImpl<>(List.of(bookingResponse)));
        when(meetingRoomAdaptor.getMeetingRoom(Mockito.anyLong())).thenReturn(meetingRoomResponse);

        bookingService.getBookingsByMember(memberInfo, Pageable.ofSize(1));

        Mockito.verify(bookingRepository, Mockito.atLeast(1)).findBookings(1L, Pageable.ofSize(1));
        Mockito.verify(meetingRoomAdaptor, Mockito.atLeast(1)).getMeetingRoom(Mockito.anyLong());
    }

    @Test
    @DisplayName("예약 전체 조회 - 페이징")
    void getAllBookings_page() {
        when(bookingRepository.findBookings(null, Pageable.ofSize(1))).thenReturn(new PageImpl<>(List.of(bookingResponse)));
        when(memberAdaptor.getMember(Mockito.anyLong())).thenReturn(memberInfo);
        when(meetingRoomAdaptor.getMeetingRoom(Mockito.anyLong())).thenReturn(meetingRoomResponse);

        bookingService.getAllBookings(Pageable.ofSize(1));

        Mockito.verify(bookingRepository, Mockito.atLeast(1)).findBookings(null, Pageable.ofSize(1));
        Mockito.verify(memberAdaptor, Mockito.atLeast(1)).getMember(Mockito.anyLong());
        Mockito.verify(meetingRoomAdaptor, Mockito.atLeast(1)).getMeetingRoom(Mockito.anyLong());
    }

    @Test
    @DisplayName("예약 전체 조회 - memberNotFound")
    void getAllBookings_exception_case1() {
        when(bookingRepository.findBookings(null, Pageable.ofSize(1))).thenReturn(new PageImpl<>(List.of(bookingResponse)));
        when(memberAdaptor.getMember(Mockito.anyLong())).thenThrow(MemberNotFoundException.class);

        Pageable pageable = Pageable.ofSize(1);
        Assertions.assertThrows(MemberNotFoundException.class, () -> bookingService.getAllBookings(pageable));

        Mockito.verify(bookingRepository, Mockito.atLeast(1)).findBookings(null, Pageable.ofSize(1));
        Mockito.verify(memberAdaptor, Mockito.atLeast(1)).getMember(Mockito.anyLong());
    }

    @Test
    @DisplayName("예약 전체 조회 - meetingRoomNotFound")
    void getAllBookings_exception_case2() {
        when(bookingRepository.findBookings(null, Pageable.ofSize(1))).thenReturn(new PageImpl<>(List.of(bookingResponse)));
        when(memberAdaptor.getMember(Mockito.anyLong())).thenReturn(memberInfo);
        when(meetingRoomAdaptor.getMeetingRoom(Mockito.anyLong())).thenThrow(MeetingRoomNotFoundException.class);

        Pageable pageable = Pageable.ofSize(1);
        Assertions.assertThrows(MeetingRoomNotFoundException.class, () -> bookingService.getAllBookings(pageable));

        Mockito.verify(bookingRepository, Mockito.atLeast(1)).findBookings(null, Pageable.ofSize(1));
        Mockito.verify(memberAdaptor, Mockito.atLeast(1)).getMember(Mockito.anyLong());
        Mockito.verify(meetingRoomAdaptor, Mockito.atLeast(1)).getMeetingRoom(Mockito.anyLong());
    }

    @Test
    @DisplayName("예약 날짜별 조회")
    void getDailyBookings() {

        DailyBookingResponse response = new DailyBookingResponse(1L, LocalDateTime.parse("2025-04-29T09:30:00"), LocalDateTime.parse("2025-04-29T10:30:00"));
        when(bookingRepository.findBookingsByDate(Mockito.anyLong(), Mockito.any())).thenReturn(List.of(response));

        List<DailyBookingResponse> responseList =  bookingService.getDailyBookings(1L, LocalDate.parse("2025-04-29"));

        Mockito.verify(bookingRepository, Mockito.times(1)).findBookingsByDate(Mockito.anyLong(), Mockito.any());

        Assertions.assertNotNull(responseList);
        Assertions.assertEquals(1L, responseList.getFirst().getNo());
    }

    @Test
    @DisplayName("예약 수정")
    void updateBooking() {
        BookingUpdateRequest request = new BookingUpdateRequest("2025-04-29", "11:30", 10, 1L);

        Booking booking = Booking.ofNewBooking("test", LocalDateTime.parse("2025-04-29T09:30:00"), 8, LocalDateTime.parse("2025-04-29T10:30:00"), 1L, null, 1L);
        ReflectionTestUtils.setField(booking, "bookingNo", 1L);

        BookingChange bookingChange = new BookingChange("변경");
        ReflectionTestUtils.setField(bookingChange, "no", 4L);

        when(bookingRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(booking));
        when(meetingRoomAdaptor.getMeetingRoom(Mockito.anyLong())).thenReturn(meetingRoomResponse);
        when(bookingChangeRepository.findById(BookingChangeType.CHANGE.getId())).thenReturn(Optional.of(bookingChange));

        bookingService.updateBooking(1L, request, memberInfo);

        Mockito.verify(bookingRepository, Mockito.times(1)).findById(Mockito.anyLong());
        Mockito.verify(meetingRoomAdaptor, Mockito.times(1)).getMeetingRoom(Mockito.anyLong());
        Mockito.verify(bookingChangeRepository, Mockito.times(1)).findById(BookingChangeType.CHANGE.getId());
    }

    @Test
    @DisplayName("예약 수정 - not found")
    void updateBooking_exception_case() {
        BookingUpdateRequest request = new BookingUpdateRequest("2025-04-29", "11:30", 10, 1L);

        Booking booking = Booking.ofNewBooking("test", LocalDateTime.parse("2025-04-29T09:30:00"), 8, LocalDateTime.parse("2025-04-29T10:30:00"), 1L, null, 1L);
        ReflectionTestUtils.setField(booking, "bookingNo", 1L);

        when(bookingRepository.findById(Mockito.anyLong())).thenReturn(Optional.empty());

        Assertions.assertThrows(BookingNotFoundException.class, () -> bookingService.updateBooking(1L, request, memberInfo));

        Mockito.verify(bookingRepository, Mockito.times(1)).findById(Mockito.anyLong());
    }

    @Test
    @DisplayName("예약 연장")
    void extendBooking() {
        Booking booking = Booking.ofNewBooking("test", LocalDateTime.parse("2025-04-29T09:30:00"), 8, LocalDateTime.parse("2025-04-29T10:30:00"), 1L, null, 1L);
        ReflectionTestUtils.setField(booking, "bookingNo", 1L);

        BookingChange bookingChange = new BookingChange("연장");
        ReflectionTestUtils.setField(bookingChange, "no", 1L);

        when(bookingRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(booking));
        when(bookingChangeRepository.findById(BookingChangeType.EXTEND.getId())).thenReturn(Optional.of(bookingChange));

        bookingService.extendBooking(1L);

        Mockito.verify(bookingRepository, Mockito.times(1)).findById(Mockito.anyLong());
        Mockito.verify(bookingChangeRepository, Mockito.times(1)).findById(BookingChangeType.EXTEND.getId());

    }

    @Test
    @DisplayName("예약 연장 실패 - already")
    void extendBooking_exception_case1() {
        Booking booking = Booking.ofNewBooking("test", LocalDateTime.parse("2025-04-29T09:30:00"), 8, LocalDateTime.parse("2025-04-29T10:30:00"), 1L, null, 1L);
        ReflectionTestUtils.setField(booking, "bookingNo", 1L);

        BookingChange bookingChange = new BookingChange("연장");
        ReflectionTestUtils.setField(bookingChange, "no", 1L);

        when(bookingRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(booking));
        when(bookingRepository.existsBooking(Mockito.anyLong(), Mockito.any())).thenReturn(true);

        Assertions.assertThrows(AlreadyMeetingRoomTimeException.class, () -> bookingService.extendBooking(1L));

        Mockito.verify(bookingRepository, Mockito.times(1)).findById(Mockito.anyLong());
        Mockito.verify(bookingRepository, Mockito.times(1)).existsBooking(Mockito.anyLong(), Mockito.any());
    }

    @Test
    @DisplayName("예약 종료")
    void finishBooking() {
        Booking booking = Booking.ofNewBooking("test", LocalDateTime.parse("2025-04-29T09:30:00"), 8, LocalDateTime.parse("2025-04-29T10:30:00"), 1L, null, 1L);
        ReflectionTestUtils.setField(booking, "bookingNo", 1L);

        BookingChange bookingChange = new BookingChange("종료");
        ReflectionTestUtils.setField(bookingChange, "no", 2L);

        when(bookingRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(booking));
        when(bookingChangeRepository.findById(BookingChangeType.FINISH.getId())).thenReturn(Optional.of(bookingChange));

        bookingService.finishBooking(1L);

        Mockito.verify(bookingRepository, Mockito.times(1)).findById(Mockito.anyLong());
        Mockito.verify(bookingChangeRepository, Mockito.times(1)).findById(BookingChangeType.FINISH.getId());

    }

    @Test
    @DisplayName("예약 취소")
    void cancelBooking() {
        Booking booking = Booking.ofNewBooking("test", LocalDateTime.parse("2025-04-29T09:30:00"), 8, LocalDateTime.parse("2025-04-29T10:30:00"), 1L, null, 1L);
        ReflectionTestUtils.setField(booking, "bookingNo", 1L);

        BookingChange bookingChange = new BookingChange("취소");
        ReflectionTestUtils.setField(bookingChange, "no", 3L);

        when(bookingRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(booking));
        when(bookingChangeRepository.findById(BookingChangeType.CANCEL.getId())).thenReturn(Optional.of(bookingChange));

        bookingService.cancelBooking(1L, memberInfo);

        Mockito.verify(bookingRepository, Mockito.times(1)).findById(Mockito.anyLong());
        Mockito.verify(bookingChangeRepository, Mockito.times(1)).findById(BookingChangeType.CANCEL.getId());
        Mockito.verify(publisher, Mockito.times(1)).publishEvent(Mockito.any(BookingCancelEvent.class));
    }

    @Test
    @DisplayName("본인인증 성공")
    void verify() {
        ConfirmPasswordRequest request = new ConfirmPasswordRequest("Test123!");
        when(bookingRepository.findByNo(Mockito.anyLong())).thenReturn(Optional.of(bookingResponse));
        when(memberAdaptor.verify(Mockito.anyLong(), Mockito.any())).thenReturn(true);

        bookingService.verify(1L, request, memberInfo);

        Mockito.verify(bookingRepository, Mockito.times(1)).findByNo(Mockito.anyLong());
        Mockito.verify(memberAdaptor, Mockito.times(1)).verify(Mockito.anyLong(), Mockito.any());
    }

    @Test
    @DisplayName("본인인증 실패 - bad request")
    void verify_exception_case() {
        ConfirmPasswordRequest request = new ConfirmPasswordRequest("test123!");

        when(bookingRepository.findByNo(Mockito.anyLong())).thenReturn(Optional.of(bookingResponse));
        when(memberAdaptor.verify(1L, request)).thenReturn(false);

        Assertions.assertThrows(BadRequestException.class, () -> bookingService.verify(1L, request, memberInfo));

        Mockito.verify(bookingRepository, Mockito.times(1)).findByNo(Mockito.anyLong());
        Mockito.verify(memberAdaptor, Mockito.times(1)).verify(1L, request);
    }
}