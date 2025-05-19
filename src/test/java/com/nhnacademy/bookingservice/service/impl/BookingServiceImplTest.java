package com.nhnacademy.bookingservice.service.impl;

import com.nhnacademy.bookingservice.common.adaptor.MeetingRoomAdaptor;
import com.nhnacademy.bookingservice.common.adaptor.MemberAdaptor;
import com.nhnacademy.bookingservice.common.event.BookingCancelEvent;
import com.nhnacademy.bookingservice.common.event.BookingCreatedEvent;
import com.nhnacademy.bookingservice.common.exception.BadRequestException;
import com.nhnacademy.bookingservice.common.exception.ForbiddenException;
import com.nhnacademy.bookingservice.common.exception.booking.*;
import com.nhnacademy.bookingservice.common.exception.NotFoundException;
import com.nhnacademy.bookingservice.common.exception.meeting.MeetingRoomCapacityExceededException;
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
import static org.junit.jupiter.api.Assertions.*;


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

        BookingResponse.MemberInfo member = new BookingResponse.MemberInfo();
        member.setNo(1L);
        member.setEmail("test@test.com");
        member.setName("test");

        BookingResponse.MeetingRoomInfo room = new BookingResponse.MeetingRoomInfo();
        room.setNo(1L);
        room.setName("회의실 A");

        bookingResponse = new BookingResponse(booking.getBookingNo(), booking.getBookingCode(), booking.getBookingDate(), booking.getAttendeeCount(), booking.getFinishesAt(), booking.getCreatedAt(), null, member, room);

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

        assertThrows(ForbiddenException.class, () -> bookingService.getBooking(1L, member));

        Mockito.verify(bookingRepository, Mockito.times(1)).findByNo(Mockito.anyLong());
    }

    @Test
    @DisplayName("예약 조회 - booking not found")
    void getBooking_exception_case2() {
        MemberResponse member = new MemberResponse(2L, "test2", "test2@test.com", "010-1111-1111", "ROLE_USER");

        when(bookingRepository.findByNo(Mockito.anyLong())).thenReturn(Optional.empty());

        assertThrows(BookingNotFoundException.class, () -> bookingService.getBooking(1L, member));

        Mockito.verify(bookingRepository, Mockito.times(1)).findByNo(Mockito.anyLong());
    }

    @Test
    @DisplayName("예약 조회 - meeting not found")
    void getBooking_exception_case3() {
        when(bookingRepository.findByNo(Mockito.anyLong())).thenReturn(Optional.of(bookingResponse));
        when(meetingRoomAdaptor.getMeetingRoom(Mockito.anyLong())).thenThrow(NotFoundException.class);

        Assertions.assertThrows(NotFoundException.class, () -> bookingService.getBooking(1L, memberInfo));

        Mockito.verify(bookingRepository, Mockito.times(1)).findByNo(Mockito.anyLong());
        Mockito.verify(meetingRoomAdaptor, Mockito.times(1)).getMeetingRoom(Mockito.anyLong());
    }

    @Test
    @DisplayName("예약 사용자별 조회 - 리스트")
    void getBookingsByMember_list() {
        when(bookingRepository.findBookingList(1L)).thenReturn(List.of(bookingResponse));
        when(meetingRoomAdaptor.getMeetingRoom(Mockito.anyLong())).thenReturn(meetingRoomResponse);

        bookingService.getMemberBookings(memberInfo);

        Mockito.verify(bookingRepository, Mockito.atLeast(1)).findBookingList(1L);
        Mockito.verify(meetingRoomAdaptor, Mockito.atLeast(1)).getMeetingRoom(Mockito.anyLong());
    }

    @Test
    @DisplayName("예약 전체 조회 - 리스트")
    void getAllBookings_list() {
        when(bookingRepository.findBookingList(null)).thenReturn(List.of(bookingResponse));
        when(memberAdaptor.getMemberByMbNo(Mockito.anyLong())).thenReturn(memberInfo);
        when(meetingRoomAdaptor.getMeetingRoom(Mockito.anyLong())).thenReturn(meetingRoomResponse);

        bookingService.getBookings();

        Mockito.verify(bookingRepository, Mockito.atLeast(1)).findBookingList(null);
        Mockito.verify(memberAdaptor, Mockito.atLeast(1)).getMemberByMbNo(Mockito.anyLong());
        Mockito.verify(meetingRoomAdaptor, Mockito.atLeast(1)).getMeetingRoom(Mockito.anyLong());
    }

    @Test
    @DisplayName("예약 사용자별 조회 - 페이징")
    void getBookingsByMember_page() {
        when(bookingRepository.findBookings(1L, Pageable.ofSize(1))).thenReturn(new PageImpl<>(List.of(bookingResponse)));
        when(meetingRoomAdaptor.getMeetingRoom(Mockito.anyLong())).thenReturn(meetingRoomResponse);

        bookingService.getPagedMemberBookings(memberInfo, Pageable.ofSize(1));

        Mockito.verify(bookingRepository, Mockito.atLeast(1)).findBookings(1L, Pageable.ofSize(1));
        Mockito.verify(meetingRoomAdaptor, Mockito.atLeast(1)).getMeetingRoom(Mockito.anyLong());
    }

    @Test
    @DisplayName("예약 전체 조회 - 페이징")
    void getAllBookings_page() {
        when(bookingRepository.findBookings(null, Pageable.ofSize(1))).thenReturn(new PageImpl<>(List.of(bookingResponse)));
        when(memberAdaptor.getMemberByMbNo(Mockito.anyLong())).thenReturn(memberInfo);
        when(meetingRoomAdaptor.getMeetingRoom(Mockito.anyLong())).thenReturn(meetingRoomResponse);

        bookingService.getPagedBookings(Pageable.ofSize(1));

        Mockito.verify(bookingRepository, Mockito.atLeast(1)).findBookings(null, Pageable.ofSize(1));
        Mockito.verify(memberAdaptor, Mockito.atLeast(1)).getMemberByMbNo(Mockito.anyLong());
        Mockito.verify(meetingRoomAdaptor, Mockito.atLeast(1)).getMeetingRoom(Mockito.anyLong());
    }

    @Test
    @DisplayName("예약 전체 조회 - memberNotFound")
    void getAllBookings_exception_case1() {
        when(bookingRepository.findBookings(null, Pageable.ofSize(1))).thenReturn(new PageImpl<>(List.of(bookingResponse)));
        when(memberAdaptor.getMemberByMbNo(Mockito.anyLong())).thenThrow(NotFoundException.class);

        Pageable pageable = Pageable.ofSize(1);

        Assertions.assertThrows(NotFoundException.class, () -> bookingService.getPagedBookings(pageable));

        Mockito.verify(bookingRepository, Mockito.atLeast(1)).findBookings(null, Pageable.ofSize(1));
        Mockito.verify(memberAdaptor, Mockito.atLeast(1)).getMemberByMbNo(Mockito.anyLong());
    }

    @Test
    @DisplayName("예약 전체 조회 - meetingRoomNotFound")
    void getAllBookings_exception_case2() {
        when(bookingRepository.findBookings(null, Pageable.ofSize(1))).thenReturn(new PageImpl<>(List.of(bookingResponse)));
        when(memberAdaptor.getMemberByMbNo(Mockito.anyLong())).thenReturn(memberInfo);
        when(meetingRoomAdaptor.getMeetingRoom(Mockito.anyLong())).thenThrow(NotFoundException.class);

        Pageable pageable = Pageable.ofSize(1);

        Assertions.assertThrows(NotFoundException.class, () -> bookingService.getPagedBookings(pageable));

        Mockito.verify(bookingRepository, Mockito.atLeast(1)).findBookings(null, Pageable.ofSize(1));
        Mockito.verify(memberAdaptor, Mockito.atLeast(1)).getMemberByMbNo(Mockito.anyLong());
        Mockito.verify(meetingRoomAdaptor, Mockito.atLeast(1)).getMeetingRoom(Mockito.anyLong());
    }

    @Test
    @DisplayName("예약 날짜별 조회")
    void getDailyBookings() {

        DailyBookingResponse response = new DailyBookingResponse(1L, 1L, 8, LocalDateTime.parse("2025-04-29T09:30:00"), LocalDateTime.parse("2025-04-29T10:30:00"));
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
    void updateBooking_exception_case1() {
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
        when(bookingRepository.existsRoomNoAndDate(Mockito.anyLong(), Mockito.any())).thenReturn(true);

        Assertions.assertThrows(AlreadyMeetingRoomTimeException.class, () -> bookingService.extendBooking(1L));

        Mockito.verify(bookingRepository, Mockito.times(1)).findById(Mockito.anyLong());
        Mockito.verify(bookingRepository, Mockito.times(1)).existsRoomNoAndDate(Mockito.anyLong(), Mockito.any());
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
    @DisplayName("예약 종료 실패")
    void finishBooking_exception_case1() {
        Booking booking = Booking.ofNewBooking("test", LocalDateTime.parse("2025-04-29T09:30:00"), 8, LocalDateTime.parse("2025-04-29T10:30:00"), 1L, null, 1L);
        ReflectionTestUtils.setField(booking, "bookingNo", 1L);

        when(bookingRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(booking));
        when(bookingChangeRepository.findById(BookingChangeType.FINISH.getId())).thenReturn(Optional.empty());

        Assertions.assertThrows(BookingChangeNotFoundException.class, () -> bookingService.finishBooking(1L));

        Mockito.verify(bookingRepository, Mockito.times(1)).findById(Mockito.anyLong());
        Mockito.verify(bookingChangeRepository, Mockito.times(1)).findById(BookingChangeType.FINISH.getId());

    }

    @Test
    @DisplayName("예약 취소 - 관리자")
    void cancelBooking_admin() {
        MemberResponse adminInfo = new MemberResponse(2L, "admin", "admin@test.com", "010-1111-1111", "ROLE_ADMIN");

        Booking booking = Booking.ofNewBooking("test", LocalDateTime.parse("2025-04-29T09:30:00"), 8, LocalDateTime.parse("2025-04-29T10:30:00"), 1L, null, 1L);
        ReflectionTestUtils.setField(booking, "bookingNo", 1L);

        BookingChange bookingChange = new BookingChange("취소");
        ReflectionTestUtils.setField(bookingChange, "no", 3L);

        when(bookingRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(booking));
        when(bookingChangeRepository.findById(BookingChangeType.CANCEL.getId())).thenReturn(Optional.of(bookingChange));

        bookingService.cancelBooking(1L, adminInfo);

        Mockito.verify(bookingRepository, Mockito.times(1)).findById(Mockito.anyLong());
        Mockito.verify(bookingChangeRepository, Mockito.times(1)).findById(BookingChangeType.CANCEL.getId());
        Mockito.verify(publisher, Mockito.times(1)).publishEvent(Mockito.any(BookingCancelEvent.class));
    }

    @Test
    @DisplayName("예약 취소 - 일반 사용자")
    void cancelBooking_user() {
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

    @Test
    @DisplayName("회의실 입실")
    void checkBooking() {
        // given
        Long no = 1L;
        String code = "testCode";
        LocalDateTime date = LocalDateTime.parse("2025-04-29T09:30:00");
        Integer attendeeCount = 8;
        LocalDateTime finishesAt = LocalDateTime.parse("2025-04-29T10:30:00");
        Long mbNo = 1L;
        Long roomNo = 1L;

        BookingResponse response = new BookingResponse(
                no,
                code,
                date,
                attendeeCount,
                finishesAt,
                LocalDateTime.now(),
                null,
                mbNo,
                roomNo
        );

        when(bookingRepository.findByNo(Mockito.anyLong())).thenReturn(Optional.of(response));

        // when
        boolean result = bookingService.checkBooking(no, code, LocalDateTime.parse("2025-04-29T09:20:00"), roomNo);

        // then
        assertTrue(result);
    }

    @Test
    @DisplayName("예약 코드 불일치 시 BookingCodeDoesNotMatchException 발생")
    void checkBookingCodeFailed() {
        // given
        Long no = 1L;
        String code = "testCode";
        LocalDateTime date = LocalDateTime.parse("2025-04-29T09:30:00");
        Integer attendeeCount = 8;
        LocalDateTime finishesAt = LocalDateTime.parse("2025-04-29T10:30:00");
        Long mbNo = 1L;
        Long roomNo = 1L;

        BookingResponse response = new BookingResponse(
                no,
                code,
                date,
                attendeeCount,
                finishesAt,
                LocalDateTime.now(),
                null,
                mbNo,
                roomNo
        );

        when(bookingRepository.findByNo(no)).thenReturn(Optional.of(response));

        // when
        // 불일치하는 예약코드 제공
        String wrongCode = "wrongCode";
        // 입실 5분 전으로 설정
        LocalDateTime entryTime = date.minusMinutes(5);

        // then
        assertThrows(BookingInfoDoesNotMatchException.class, () ->
                bookingService.checkBooking(1L, wrongCode, entryTime, roomNo)
        );
    }

    @Test
    @DisplayName("입실 날짜가 예약 날짜와 다른 경우 - 입실 불가")
    void checkBookingDifferentDate() {
        // given
        Long no = 1L;
        String code = "testCode";
        LocalDateTime date = LocalDateTime.parse("2025-04-29T09:30:00");
        Integer attendeeCount = 8;
        LocalDateTime finishesAt = LocalDateTime.parse("2025-04-29T10:30:00");
        Long mbNo = 1L;
        Long roomNo = 1L;

        BookingResponse response = new BookingResponse(
                no,
                code,
                date,
                attendeeCount,
                finishesAt,
                LocalDateTime.now(),
                null,
                mbNo,
                roomNo
        );

        when(bookingRepository.findByNo(no)).thenReturn(Optional.of(response));

        // when
        LocalDateTime differentDateEntry = LocalDateTime.parse("2025-04-30T09:30:00");

        // then
        assertThrows(BookingInfoDoesNotMatchException.class, () ->
                bookingService.checkBooking(no, code, differentDateEntry, roomNo)
        );
    }

    @Test
    @DisplayName("입실이 예약시간보다 10분 이상 빠른 경우 - 입실 불가")
    void checkBookingEarlyEntry() {
        // given
        Long no = 1L;
        String code = "testCode";
        LocalDateTime date = LocalDateTime.parse("2025-04-29T09:30:00");
        Integer attendeeCount = 8;
        LocalDateTime finishesAt = LocalDateTime.parse("2025-04-29T10:30:00");
        Long mbNo = 1L;
        Long roomNo = 1L;

        BookingResponse response = new BookingResponse(
                no,
                code,
                date,
                attendeeCount,
                finishesAt,
                LocalDateTime.now(),
                null,
                mbNo,
                roomNo
        );

        when(bookingRepository.findByNo(no)).thenReturn(Optional.of(response));

        // when
        // 예약 시간 15분 전 입실 시도
        LocalDateTime earlyEntry = date.minusMinutes(15);

        assertThrows(BookingTimeNotReachedException.class, () ->
                bookingService.checkBooking(no, code, earlyEntry, roomNo)
        );
    }

    @Test
    @DisplayName("입실이 예약시간보다 10분 이상 늦은 경우 - 입실 불가")
    void checkBookingLateEntry() {
        // given
        Long no = 1L;
        String code = "testCode";
        LocalDateTime date = LocalDateTime.parse("2025-04-29T09:30:00");
        Integer attendeeCount = 8;
        LocalDateTime finishesAt = LocalDateTime.parse("2025-04-29T10:30:00");
        Long mbNo = 1L;
        Long roomNo = 1L;

        BookingResponse response = new BookingResponse(
                no,
                code,
                date,
                attendeeCount,
                finishesAt,
                LocalDateTime.now(),
                null,
                mbNo,
                roomNo
        );

        when(bookingRepository.findByNo(no)).thenReturn(Optional.of(response));

        // when
        // 예약 시간 15분 이후 입실 시도
        LocalDateTime lateEntry = date.plusMinutes(15);

        assertThrows(BookingTimeHasPassedException.class, () ->
                bookingService.checkBooking(no, code, lateEntry, roomNo)
        );
    }

}