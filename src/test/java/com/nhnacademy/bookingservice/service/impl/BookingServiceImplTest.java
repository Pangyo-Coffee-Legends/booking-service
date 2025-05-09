package com.nhnacademy.bookingservice.service.impl;

import com.nhnacademy.bookingservice.common.adaptor.MeetingRoomAdaptor;
import com.nhnacademy.bookingservice.common.adaptor.MemberAdaptor;
import com.nhnacademy.bookingservice.common.auth.MemberThreadLocal;
import com.nhnacademy.bookingservice.common.exception.ForbiddenException;
import com.nhnacademy.bookingservice.common.exception.booking.*;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
        MemberThreadLocal.setMemberNoLocal(1L);

        memberInfo = new MemberResponse(1L, "test");
        meetingRoomResponse = new MeetingRoomResponse(1L, "회의실 A", 5);

        Booking booking = Booking.ofNewBooking("test", LocalDateTime.parse("2025-04-29T09:30:00"), 8, LocalDateTime.parse("2025-04-29T10:30:00"), 1L, null, 2L);
        ReflectionTestUtils.setField(booking, "bookingNo", 1L);

        bookingResponse = new BookingResponse(booking.getBookingNo(), booking.getBookingCode(), booking.getBookingDate(), booking.getAttendeeCount(), booking.getFinishedAt(), booking.getCreatedAt(), booking.getMbNo(),  null, booking.getRoomNo());

    }

    @AfterEach
    void tearDown() {
        MemberThreadLocal.removeMemberNo();
    }

    @Test
    @DisplayName("예약 생성 성공")
    void register() {
        BookingRegisterRequest request = new BookingRegisterRequest(1L, "2025-04-29", "09:30", 6);
        MeetingRoomResponse roomResponse = new MeetingRoomResponse(1L, "회의실 A", 6);
        when(meetingRoomAdaptor.getMeetingRoom(Mockito.anyLong())).thenReturn(ResponseEntity.ok(roomResponse));
        when(bookingRepository.existsRoomNoAndDate(Mockito.anyLong(), Mockito.any())).thenReturn(false);

        bookingService.register(request);

        verify(bookingRepository, Mockito.times(1)).save(Mockito.any(Booking.class));
    }

    @Test
    @DisplayName("예약 생성 실패 - 수용인원 초과")
    void register_exception_case1() {
        BookingRegisterRequest request = new BookingRegisterRequest(1L, "2025-04-29", "09:30", 8);
        MeetingRoomResponse roomResponse = new MeetingRoomResponse(1L, "회의실 A", 6);

        when(meetingRoomAdaptor.getMeetingRoom(Mockito.anyLong())).thenReturn(ResponseEntity.ok(roomResponse));
        assertThrows(MeetingRoomCapacityExceededException.class, () -> bookingService.register(request));

        verify(bookingRepository, Mockito.never()).save(Mockito.any(Booking.class));
    }

    @Test
    @DisplayName("예약 생성 실패 - 예약 중복")
    void register_exception_case2() {
        BookingRegisterRequest request = new BookingRegisterRequest(1L, "2025-04-29", "09:30", 5);
        MeetingRoomResponse roomResponse = new MeetingRoomResponse(1L, "회의실 A", 6);

        when(meetingRoomAdaptor.getMeetingRoom(Mockito.anyLong())).thenReturn(ResponseEntity.ok(roomResponse));
        when(bookingRepository.existsRoomNoAndDate(Mockito.anyLong(), Mockito.any())).thenReturn(true);

        assertThrows(AlreadyMeetingRoomTimeException.class, () -> bookingService.register(request));

        verify(bookingRepository, Mockito.never()).save(Mockito.any(Booking.class));
    }

    @Test
    @DisplayName("예약 조회")
    void getBooking() {
        when(bookingRepository.findByNo(Mockito.anyLong())).thenReturn(Optional.of(bookingResponse));
        when(meetingRoomAdaptor.getMeetingRoom(Mockito.anyLong())).thenReturn(ResponseEntity.ok(meetingRoomResponse));

        bookingService.getBooking(1L, memberInfo);

        verify(bookingRepository, Mockito.times(1)).findByNo(Mockito.anyLong());
        verify(meetingRoomAdaptor, Mockito.times(1)).getMeetingRoom(Mockito.anyLong());
    }

    @Test
    @DisplayName("예약 조회 - forbidden")
    void getBooking_exception_case1() {
         MemberResponse member = new MemberResponse(2L, "test2");

        when(bookingRepository.findByNo(Mockito.anyLong())).thenReturn(Optional.of(bookingResponse));

        assertThrows(ForbiddenException.class, () -> bookingService.getBooking(1L, member));

        verify(bookingRepository, Mockito.times(1)).findByNo(Mockito.anyLong());
    }

    @Test
    @DisplayName("예약 조회 - booking not found")
    void getBooking_exception_case2() {
        MemberResponse member = new MemberResponse(2L, "test2");

        when(bookingRepository.findByNo(Mockito.anyLong())).thenReturn(Optional.empty());

        assertThrows(BookingNotFoundException.class, () -> bookingService.getBooking(1L, member));

        verify(bookingRepository, Mockito.times(1)).findByNo(Mockito.anyLong());
    }

    @Test
    @DisplayName("예약 조회 - meeting not found")
    void getBooking_exception_case3() {
        when(bookingRepository.findByNo(Mockito.anyLong())).thenReturn(Optional.of(bookingResponse));
        when(meetingRoomAdaptor.getMeetingRoom(Mockito.anyLong())).thenReturn(ResponseEntity.notFound().build());

        assertThrows(MeetingRoomNotFoundException.class, () -> bookingService.getBooking(1L, memberInfo));

        verify(bookingRepository, Mockito.times(1)).findByNo(Mockito.anyLong());
        verify(meetingRoomAdaptor, Mockito.times(1)).getMeetingRoom(Mockito.anyLong());
    }

    @Test
    @DisplayName("예약 조회 - meeting not found(null)")
    void getBooking_exception_case4() {
        when(bookingRepository.findByNo(Mockito.anyLong())).thenReturn(Optional.of(bookingResponse));
        when(meetingRoomAdaptor.getMeetingRoom(Mockito.anyLong())).thenReturn(ResponseEntity.ok().build());

        assertThrows(MeetingRoomNotFoundException.class, () -> bookingService.getBooking(1L, memberInfo));

        verify(bookingRepository, Mockito.times(1)).findByNo(Mockito.anyLong());
        verify(meetingRoomAdaptor, Mockito.times(1)).getMeetingRoom(Mockito.anyLong());
    }

    @Test
    @DisplayName("예약 사용자별 조회")
    void getBookingsByMember() {
        when(bookingRepository.findBookings(1L, Pageable.ofSize(1))).thenReturn(new PageImpl<>(List.of(bookingResponse)));
        when(meetingRoomAdaptor.getMeetingRoom(Mockito.anyLong())).thenReturn(ResponseEntity.ok(meetingRoomResponse));

        bookingService.getBookingsByMember(memberInfo, Pageable.ofSize(1));

        verify(bookingRepository, Mockito.atLeast(1)).findBookings(1L, Pageable.ofSize(1));
        verify(meetingRoomAdaptor, Mockito.atLeast(1)).getMeetingRoom(Mockito.anyLong());
    }

    @Test
    @DisplayName("예약 전체 조회")
    void getAllBookings() {
        when(bookingRepository.findBookings(null, Pageable.ofSize(1))).thenReturn(new PageImpl<>(List.of(bookingResponse)));
        when(memberAdaptor.getMemberName(Mockito.anyLong())).thenReturn(ResponseEntity.ok(memberInfo));
        when(meetingRoomAdaptor.getMeetingRoom(Mockito.anyLong())).thenReturn(ResponseEntity.ok(meetingRoomResponse));

        bookingService.getAllBookings(Pageable.ofSize(1));

        verify(bookingRepository, Mockito.atLeast(1)).findBookings(null, Pageable.ofSize(1));
        verify(memberAdaptor, Mockito.atLeast(1)).getMemberName(Mockito.anyLong());
        verify(meetingRoomAdaptor, Mockito.atLeast(1)).getMeetingRoom(Mockito.anyLong());
    }

    @Test
    @DisplayName("예약 전체 조회 - memberNotFound")
    void getAllBookings_exception_case1() {
        when(bookingRepository.findBookings(null, Pageable.ofSize(1))).thenReturn(new PageImpl<>(List.of(bookingResponse)));
        when(memberAdaptor.getMemberName(Mockito.anyLong())).thenReturn(ResponseEntity.notFound().build());

        Pageable pageable = Pageable.ofSize(1);
        assertThrows(MemberNotFoundException.class, () -> bookingService.getAllBookings(pageable));

        verify(bookingRepository, Mockito.atLeast(1)).findBookings(null, Pageable.ofSize(1));
        verify(memberAdaptor, Mockito.atLeast(1)).getMemberName(Mockito.anyLong());
    }

    @Test
    @DisplayName("예약 전체 조회 - meetingRoomNotFound")
    void getAllBookings_exception_case2() {
        when(bookingRepository.findBookings(null, Pageable.ofSize(1))).thenReturn(new PageImpl<>(List.of(bookingResponse)));
        when(memberAdaptor.getMemberName(Mockito.anyLong())).thenReturn(ResponseEntity.ok(memberInfo));
        when(meetingRoomAdaptor.getMeetingRoom(Mockito.anyLong())).thenReturn(ResponseEntity.notFound().build());

        Pageable pageable = Pageable.ofSize(1);
        assertThrows(MeetingRoomNotFoundException.class, () -> bookingService.getAllBookings(pageable));

        verify(bookingRepository, Mockito.atLeast(1)).findBookings(null, Pageable.ofSize(1));
        verify(memberAdaptor, Mockito.atLeast(1)).getMemberName(Mockito.anyLong());
        verify(meetingRoomAdaptor, Mockito.atLeast(1)).getMeetingRoom(Mockito.anyLong());
    }

    @Test
    @DisplayName("예약 날짜별 조회")
    void getDailyBookings() {

        DailyBookingResponse response = new DailyBookingResponse(1L, LocalDateTime.parse("2025-04-29T09:30:00"), LocalDateTime.parse("2025-04-29T10:30:00"));
        when(bookingRepository.findBookingsByDate(Mockito.anyLong(), Mockito.any())).thenReturn(List.of(response));

        List<DailyBookingResponse> responseList =  bookingService.getDailyBookings(1L, LocalDate.parse("2025-04-29"));

        verify(bookingRepository, Mockito.times(1)).findBookingsByDate(Mockito.anyLong(), Mockito.any());

        Assertions.assertNotNull(responseList);
        Assertions.assertEquals(1L, responseList.getFirst().getNo());
    }

    @Test
    @DisplayName("예약 수정")
    void updateBooking() {
        MemberThreadLocal.setMemberNoLocal(1L);
        BookingUpdateRequest request = new BookingUpdateRequest("2025-04-29", "11:30", 10);

        Booking booking = Booking.ofNewBooking("test", LocalDateTime.parse("2025-04-29T09:30:00"), 8, LocalDateTime.parse("2025-04-29T10:30:00"), 1L, null, 1L);
        ReflectionTestUtils.setField(booking, "bookingNo", 1L);

        BookingChange bookingChange = new BookingChange("변경");
        ReflectionTestUtils.setField(bookingChange, "no", 4L);

        when(bookingRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(booking));
        when(memberAdaptor.getMemberName(1L)).thenReturn(ResponseEntity.ok(memberInfo));
        when(meetingRoomAdaptor.getMeetingRoom(Mockito.anyLong())).thenReturn(ResponseEntity.ok(meetingRoomResponse));
        when(bookingChangeRepository.findById(BookingChangeType.CHANGE.getId())).thenReturn(Optional.of(bookingChange));

        bookingService.updateBooking(1L, request);

        verify(bookingRepository, Mockito.times(1)).findById(Mockito.anyLong());
        verify(memberAdaptor, Mockito.atLeast(1)).getMemberName(Mockito.anyLong());
        verify(meetingRoomAdaptor, Mockito.times(1)).getMeetingRoom(Mockito.anyLong());
        verify(bookingChangeRepository, Mockito.times(1)).findById(BookingChangeType.CHANGE.getId());
    }

    @Test
    @DisplayName("예약 수정 - not found")
    void updateBooking_exception_case() {
        MemberThreadLocal.setMemberNoLocal(1L);
        BookingUpdateRequest request = new BookingUpdateRequest("2025-04-29", "11:30", 10);

        Booking booking = Booking.ofNewBooking("test", LocalDateTime.parse("2025-04-29T09:30:00"), 8, LocalDateTime.parse("2025-04-29T10:30:00"), 1L, null, 1L);
        ReflectionTestUtils.setField(booking, "bookingNo", 1L);

        when(bookingRepository.findById(Mockito.anyLong())).thenReturn(Optional.empty());

        assertThrows(BookingNotFoundException.class, () -> bookingService.updateBooking(1L, request));

        verify(bookingRepository, Mockito.times(1)).findById(Mockito.anyLong());
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

        verify(bookingRepository, Mockito.times(1)).findById(Mockito.anyLong());
        verify(bookingChangeRepository, Mockito.times(1)).findById(BookingChangeType.EXTEND.getId());

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

        verify(bookingRepository, Mockito.times(1)).findById(Mockito.anyLong());
        verify(bookingChangeRepository, Mockito.times(1)).findById(BookingChangeType.FINISH.getId());

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

        verify(bookingRepository, Mockito.times(1)).findById(Mockito.anyLong());
        verify(bookingChangeRepository, Mockito.times(1)).findById(BookingChangeType.CANCEL.getId());
    }

    @Test
    @DisplayName("회의실 입실")
    void checkBooking() {
        // given
        Long no = 1L;
        String code = "testCode";
        LocalDateTime date = LocalDateTime.parse("2025-04-29T09:30:00");
        Integer attendeeCount = 8;
        LocalDateTime finishedAt = LocalDateTime.parse("2025-04-29T10:30:00");
        Long mbNo = 1L;
        Long roomNo = 1L;

        BookingResponse response = new BookingResponse(
                no,
                code,
                date,
                attendeeCount,
                finishedAt,
                LocalDateTime.now(),
                mbNo,
                null,
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
        LocalDateTime finishedAt = LocalDateTime.parse("2025-04-29T10:30:00");
        Long mbNo = 1L;
        Long roomNo = 1L;

        BookingResponse response = new BookingResponse(
                no,
                code,
                date,
                attendeeCount,
                finishedAt,
                LocalDateTime.now(),
                mbNo,
                null,
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
        LocalDateTime finishedAt = LocalDateTime.parse("2025-04-29T10:30:00");
        Long mbNo = 1L;
        Long roomNo = 1L;

        BookingResponse response = new BookingResponse(
                no,
                code,
                date,
                attendeeCount,
                finishedAt,
                LocalDateTime.now(),
                mbNo,
                null,
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
        LocalDateTime finishedAt = LocalDateTime.parse("2025-04-29T10:30:00");
        Long mbNo = 1L;
        Long roomNo = 1L;

        BookingResponse response = new BookingResponse(
                no,
                code,
                date,
                attendeeCount,
                finishedAt,
                LocalDateTime.now(),
                mbNo,
                null,
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
        LocalDateTime finishedAt = LocalDateTime.parse("2025-04-29T10:30:00");
        Long mbNo = 1L;
        Long roomNo = 1L;

        BookingResponse response = new BookingResponse(
                no,
                code,
                date,
                attendeeCount,
                finishedAt,
                LocalDateTime.now(),
                mbNo,
                null,
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