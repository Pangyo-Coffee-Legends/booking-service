package com.nhnacademy.bookingservice.service.impl;

import com.nhnacademy.bookingservice.common.adaptor.MeetingRoomAdaptor;
import com.nhnacademy.bookingservice.common.adaptor.MemberAdaptor;
import com.nhnacademy.bookingservice.common.auth.MemberThreadLocal;
import com.nhnacademy.bookingservice.common.exception.ForbiddenException;
import com.nhnacademy.bookingservice.common.exception.booking.AlreadyMeetingRoomTimeException;
import com.nhnacademy.bookingservice.common.exception.booking.BookingChangeNotFoundException;
import com.nhnacademy.bookingservice.common.exception.booking.BookingInfoDoesNotMatchException;
import com.nhnacademy.bookingservice.common.exception.booking.BookingNotFoundException;
import com.nhnacademy.bookingservice.common.exception.booking.BookingTimeHasPassedException;
import com.nhnacademy.bookingservice.common.exception.booking.BookingTimeNotReachedException;
import com.nhnacademy.bookingservice.common.exception.meeting.MeetingRoomCapacityExceededException;
import com.nhnacademy.bookingservice.common.exception.meeting.MeetingRoomNotFoundException;
import com.nhnacademy.bookingservice.common.exception.member.MemberNotFoundException;
import com.nhnacademy.bookingservice.entity.Booking;
import com.nhnacademy.bookingservice.entity.BookingChange;
import com.nhnacademy.bookingservice.dto.*;
import com.nhnacademy.bookingservice.entity.BookingChangeType;
import com.nhnacademy.bookingservice.repository.BookingChangeRepository;
import com.nhnacademy.bookingservice.repository.BookingRepository;
import com.nhnacademy.bookingservice.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


@Service
@Transactional
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService{

    private final BookingRepository bookingRepository;
    private final BookingChangeRepository bookingChangeRepository;
    private final MeetingRoomAdaptor meetingRoomAdaptor;
    private final MemberAdaptor memberAdaptor;

    @Override
    public BookingRegisterResponse register(BookingRegisterRequest request) {

        MeetingRoomResponse room = getMeetingRoom(request.getRoomNo());

        if(room.getMeetingRoomCapacity() < request.getAttendeeCount()) {
            throw new MeetingRoomCapacityExceededException(room.getMeetingRoomCapacity());
        }

        String code = UUID.randomUUID().toString().split("-")[0];

        LocalDate date = LocalDate.parse(request.getDate());
        LocalTime time = LocalTime.parse(request.getTime());
        LocalDateTime dateTime = LocalDateTime.of(date, time);

        if(bookingRepository.existsRoomNoAndDate(request.getRoomNo(), dateTime)) {
            throw new AlreadyMeetingRoomTimeException();
        }

        Booking booking = Booking.ofNewBooking(code, dateTime, request.getAttendeeCount(), dateTime.plusHours(1), MemberThreadLocal.getMemberNo(), null, request.getRoomNo());
        bookingRepository.save(booking);

        return new BookingRegisterResponse(booking.getBookingNo());
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBooking(Long no, MemberResponse memberInfo) {
        BookingResponse booking = bookingRepository.findByNo(no).orElseThrow(BookingNotFoundException::new);
        booking.setMbName(memberInfo.getName());

        checkMember(booking.getMbNo(), memberInfo.getNo());

        MeetingRoomResponse room = getMeetingRoom(booking.getRoomNo());
        booking.setRoomName(room.getMeetingRoomName());

        return booking;
    }

    @Override
    public Page<BookingResponse> getBookingsByMember(MemberResponse memberInfo, Pageable pageable) {
        Page<BookingResponse> bookings = bookingRepository.findBookings(memberInfo.getNo(), pageable);

        bookings.forEach(booking -> {
            booking.setMbName(memberInfo.getName());

            MeetingRoomResponse room = getMeetingRoom(booking.getRoomNo());
            booking.setRoomName(room.getMeetingRoomName());
        });

        return bookings;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> getAllBookings(Pageable pageable) {

        Page<BookingResponse> bookings = bookingRepository.findBookings(null, pageable);

        bookings.forEach(booking -> {
            String mbName = getMemberName(booking.getMbNo());
            booking.setMbName(mbName);

            MeetingRoomResponse room = getMeetingRoom(booking.getRoomNo());
            booking.setRoomName(room.getMeetingRoomName());
        });

        return bookings;
    }

    @Override
    public List<DailyBookingResponse> getDailyBookings(Long roomNo, LocalDate date) {
        List<DailyBookingResponse> bookings = bookingRepository.findBookingsByDate(roomNo, date);

        for (DailyBookingResponse booking : bookings) {

            MemberResponse member = memberAdaptor.getMemberByMbNo(booking.getMbNo()).getBody();

            if (member != null) {
                String mbName = member.getName();
                booking.setMbName(mbName);
            }
        }

        return bookings;
    }

    @Override
    public BookingResponse updateBooking(Long no, BookingUpdateRequest request){
        Booking booking = bookingRepository.findById(no).orElseThrow(() -> new BookingNotFoundException(no));

        checkMember(booking.getMbNo(), MemberThreadLocal.getMemberNo());

        String mbName = getMemberName(booking.getMbNo());
        MeetingRoomResponse room = getMeetingRoom(booking.getRoomNo());

        LocalDate date = LocalDate.parse(request.getDate());
        LocalTime time = LocalTime.parse(request.getTime());
        LocalDateTime dateTime = LocalDateTime.of(date, time);
        booking.update(dateTime, request.getAttendeeCount(), dateTime.plusHours(1));

        BookingChange change = bookingChangeRepository.findById(BookingChangeType.CHANGE.getId())
                .orElseThrow(() -> new BookingChangeNotFoundException(BookingChangeType.CHANGE.getId()));
        booking.updateBookingEvent(change);

        return convertBookingResponse(booking, mbName, room.getMeetingRoomName());
    }

    @Override
    public void extendBooking(Long no){
        Booking booking = bookingRepository.findById(no)
                .orElseThrow(() -> new BookingNotFoundException(no));

        BookingChange change = bookingChangeRepository.findById(BookingChangeType.EXTEND.getId())
                .orElseThrow(() -> new BookingChangeNotFoundException(BookingChangeType.EXTEND.getId()));

        booking.updateBookingEvent(change);
        booking.updateFinishesAt(booking.getFinishesAt().plusMinutes(30));

    }

    @Override
    public void finishBooking(Long no) {
        Booking booking = bookingRepository.findById(no)
                .orElseThrow(() -> new BookingNotFoundException(no));

        BookingChange change = bookingChangeRepository.findById(BookingChangeType.FINISH.getId())
                .orElseThrow(() -> new BookingChangeNotFoundException(BookingChangeType.FINISH.getId()));

        booking.updateBookingEvent(change);

    }

    @Override
    public void cancelBooking(Long no, MemberResponse memberInfo){
        Booking booking = bookingRepository.findById(no)
                .orElseThrow(() -> new BookingNotFoundException(no));
        checkMember(booking.getMbNo(), memberInfo.getNo());

        BookingChange change = bookingChangeRepository.findById(BookingChangeType.CANCEL.getId())
                .orElseThrow(() -> new BookingChangeNotFoundException(BookingChangeType.CANCEL.getId()));

        booking.updateBookingEvent(change);
        booking.updateFinishesAt(null);

        // 언제 취소 됐는지 있어야할 듯
    }

    /**
     *
     * @param no 예약번호
     * @param code 회의실 예약 시 발급 받은 예약 코드
     * @param entryTime 회의실
     * @param meetingRoomNo 회의실 번호
     * @return 회의실 입실 허가 boolean 반환
     */
    @Override
    public boolean checkBooking(Long no, String code, LocalDateTime entryTime, Long meetingRoomNo) {
        // 저장된 예약정보 찾아오기
        BookingResponse bookingResponse = bookingRepository.findByNo(no).orElseThrow(() -> new BookingNotFoundException(no));

        // 저장된 예약정보 내 예약코드
        String bookingCode = bookingResponse.getCode();

        // 예약 날짜와 회의실 입실 날짜 비교를 위한 날짜 포맷
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // 예약 날짜
        LocalDateTime bookingDateTime = bookingResponse.getDate();
        String bookingDateStr = bookingDateTime.format(dateFormatter);

        // 회의실 입실 날짜
        String entryDateStr = entryTime.format(dateFormatter);

        // 예약 시간과 입실 시간 차이
        Duration duration = Duration.between(bookingDateTime, entryTime);
        long minutes = duration.toMinutesPart();

        if (bookingCode.equals(code) && bookingDateStr.equals(entryDateStr)) {
            if (Math.abs(minutes) <= 10) { // 예약 시간과 입실 시간 차이 절댓값 10 이하
                return true;
            } else if (minutes < -10) {
                throw new BookingTimeNotReachedException();
            } else if (minutes > 10) {
                throw new BookingTimeHasPassedException();
            }
        } else {
            throw new BookingInfoDoesNotMatchException();
        }

        return false;
    }

    private BookingResponse convertBookingResponse(Booking booking, String mbName, String roomName){
        return new BookingResponse(
                booking.getBookingNo(),
                booking.getBookingCode(),
                booking.getBookingDate(),
                booking.getAttendeeCount(),
                booking.getFinishesAt(),
                booking.getCreatedAt(),
                booking.getMbNo(),
                mbName,
                booking.getBookingChange() == null ? null : booking.getBookingChange().getName(),
                booking.getRoomNo(),
                roomName
        );
    }

    private void checkMember(Long bookingMbNo, Long loginMbNo){
        if(!Objects.equals(bookingMbNo, loginMbNo)){
            throw new ForbiddenException();
        }
    }

    private MeetingRoomResponse getMeetingRoom(Long roomNo){
        ResponseEntity<MeetingRoomResponse> roomEntity = meetingRoomAdaptor.getMeetingRoom(roomNo);
        MeetingRoomResponse room = roomEntity.getBody();
        if(!roomEntity.getStatusCode().is2xxSuccessful()|| room == null){
            throw new MeetingRoomNotFoundException();
        }

        return room;
    }

    private String getMemberName(Long mbNo) {
        ResponseEntity<MemberResponse> memberEntity = memberAdaptor.getMemberName(mbNo);
        MemberResponse member = memberEntity.getBody();
        if (!memberEntity.getStatusCode().is2xxSuccessful() || member == null) {
            throw new MemberNotFoundException(mbNo);
        }

        return member.getName();
    }
}
