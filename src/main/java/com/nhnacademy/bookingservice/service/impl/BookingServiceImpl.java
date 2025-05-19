package com.nhnacademy.bookingservice.service.impl;

import com.nhnacademy.bookingservice.common.adaptor.MeetingRoomAdaptor;
import com.nhnacademy.bookingservice.common.adaptor.MemberAdaptor;
import com.nhnacademy.bookingservice.common.event.BookingCancelEvent;
import com.nhnacademy.bookingservice.common.event.BookingCreatedEvent;
import com.nhnacademy.bookingservice.common.exception.BadRequestException;
import com.nhnacademy.bookingservice.common.exception.ForbiddenException;
import com.nhnacademy.bookingservice.common.exception.booking.AlreadyMeetingRoomTimeException;
import com.nhnacademy.bookingservice.common.exception.booking.BookingChangeNotFoundException;
import com.nhnacademy.bookingservice.common.exception.booking.BookingInfoDoesNotMatchException;
import com.nhnacademy.bookingservice.common.exception.booking.BookingNotFoundException;
import com.nhnacademy.bookingservice.common.exception.booking.BookingTimeHasPassedException;
import com.nhnacademy.bookingservice.common.exception.booking.BookingTimeNotReachedException;
import com.nhnacademy.bookingservice.common.exception.meeting.MeetingRoomCapacityExceededException;
import com.nhnacademy.bookingservice.domain.Booking;
import com.nhnacademy.bookingservice.domain.BookingChange;
import com.nhnacademy.bookingservice.dto.*;
import com.nhnacademy.bookingservice.domain.BookingChangeType;
import com.nhnacademy.bookingservice.repository.BookingChangeRepository;
import com.nhnacademy.bookingservice.repository.BookingRepository;
import com.nhnacademy.bookingservice.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    private final ApplicationEventPublisher publisher;

    private final BookingRepository bookingRepository;
    private final BookingChangeRepository bookingChangeRepository;
    private final MeetingRoomAdaptor meetingRoomAdaptor;
    private final MemberAdaptor memberAdaptor;

    @Override
    public BookingRegisterResponse register(BookingRegisterRequest request, MemberResponse memberInfo) {

        MeetingRoomResponse room = getMeetingRoom(request.getRoomNo());

        if(room.getMeetingRoomCapacity() < request.getAttendeeCount()) {
            throw new MeetingRoomCapacityExceededException(room.getMeetingRoomCapacity());
        }

        String code = UUID.randomUUID().toString().split("-")[0];

        LocalDate date = LocalDate.parse(request.getDate());
        LocalTime time = LocalTime.parse(request.getTime());
        LocalDateTime dateTime = LocalDateTime.of(date, time);

        if(bookingRepository.existsRoomNoAndDate(request.getRoomNo(), dateTime)) {
            throw new AlreadyMeetingRoomTimeException(dateTime);
        }

        Booking booking = Booking.ofNewBooking(code, dateTime, request.getAttendeeCount(), dateTime.plusHours(1), memberInfo.getNo(), null, request.getRoomNo());
        bookingRepository.save(booking);

        publisher.publishEvent(new BookingCreatedEvent(this, memberInfo.getEmail(), booking.getBookingNo()));

        return new BookingRegisterResponse(booking.getBookingNo());
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBooking(Long no, MemberResponse memberInfo) {
        BookingResponse booking = bookingRepository.findByNo(no).orElseThrow(BookingNotFoundException::new);
        booking.getMember().setName(memberInfo.getName());

        checkMember(booking.getMember().getNo(), memberInfo.getNo());

        MeetingRoomResponse room = getMeetingRoom(booking.getRoom().getNo());
        booking.getRoom().setName(room.getMeetingRoomName());

        return booking;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getMemberBookings(MemberResponse memberInfo) {
        List<BookingResponse> bookings = bookingRepository.findBookingList(memberInfo.getNo());

        bookings.forEach(booking -> {
            booking.getMember().setName(memberInfo.getName());

            MeetingRoomResponse room = getMeetingRoom(booking.getRoom().getNo());
            booking.getRoom().setName(room.getMeetingRoomName());
        });
        return bookings;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getBookings() {
        List<BookingResponse> bookings = bookingRepository.findBookingList(null);

        bookings.forEach(booking -> {
            MemberResponse member = getMember(booking.getMember().getNo());
            booking.getMember().setName(member.getName());
            booking.getMember().setEmail(member.getEmail());

            MeetingRoomResponse room = getMeetingRoom(booking.getRoom().getNo());
            booking.getRoom().setName(room.getMeetingRoomName());
        });
        return bookings;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> getPagedMemberBookings(MemberResponse memberInfo, Pageable pageable) {
        Page<BookingResponse> bookings = bookingRepository.findBookings(memberInfo.getNo(), pageable);

        bookings.forEach(booking -> {
            booking.getMember().setName(memberInfo.getName());

            MeetingRoomResponse room = getMeetingRoom(booking.getRoom().getNo());
            booking.getRoom().setName(room.getMeetingRoomName());
        });

        return bookings;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> getPagedBookings(Pageable pageable) {

        Page<BookingResponse> bookings = bookingRepository.findBookings(null, pageable);

        bookings.forEach(booking -> {
            MemberResponse member = getMember(booking.getMember().getNo());
            booking.getMember().setName(member.getName());
            booking.getMember().setEmail(member.getEmail());

            MeetingRoomResponse room = getMeetingRoom(booking.getRoom().getNo());
            booking.getRoom().setName(room.getMeetingRoomName());
        });

        return bookings;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DailyBookingResponse> getDailyBookings(Long roomNo, LocalDate date) {
        List<DailyBookingResponse> bookings = bookingRepository.findBookingsByDate(roomNo, date);

        for (DailyBookingResponse booking : bookings) {

            MemberResponse member = memberAdaptor.getMemberByMbNo(booking.getMbNo());

            if (member != null) {
                String mbName = member.getName();
                booking.setMbName(mbName);
            }
        }

        return bookings;
    }

    @Override
    public BookingResponse updateBooking(Long no, BookingUpdateRequest request, MemberResponse memberInfo){
        Booking booking = bookingRepository.findById(no).orElseThrow(() -> new BookingNotFoundException(no));

        checkMember(booking.getMbNo(), memberInfo.getNo());

        MeetingRoomResponse room = getMeetingRoom(request.getRoomNo());

        LocalDate date = LocalDate.parse(request.getDate());
        LocalTime time = LocalTime.parse(request.getTime());
        LocalDateTime dateTime = LocalDateTime.of(date, time);
        booking.update(dateTime, request.getAttendeeCount(), dateTime.plusHours(1), room.getNo());

        BookingChange change = bookingChangeRepository.findById(BookingChangeType.CHANGE.getId())
                .orElseThrow(() -> new BookingChangeNotFoundException(BookingChangeType.CHANGE.getId()));
        booking.updateBookingEvent(change);

        return convertBookingResponse(booking, memberInfo.getName(), room.getMeetingRoomName());
    }

    @Override
    public void extendBooking(Long no){
        Booking booking = bookingRepository.findById(no)
                .orElseThrow(() -> new BookingNotFoundException(no));

        if(bookingRepository.existsRoomNoAndDate(booking.getRoomNo(), booking.getFinishesAt())){
            throw new AlreadyMeetingRoomTimeException();
        }

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

        if(!Objects.equals(memberInfo.getRoleName(), "ROLE_ADMIN")){
            checkMember(booking.getMbNo(), memberInfo.getNo());
        }
        BookingChange change = bookingChangeRepository.findById(BookingChangeType.CANCEL.getId())
                .orElseThrow(() -> new BookingChangeNotFoundException(BookingChangeType.CANCEL.getId()));

        booking.updateBookingEvent(change);
        booking.updateFinishesAt(null);

        publisher.publishEvent(new BookingCancelEvent(this, memberInfo.getEmail(), booking.getBookingNo()));
    }

    @Override
    public boolean verify(Long no, ConfirmPasswordRequest request, MemberResponse memberInfo) {
        BookingResponse booking = bookingRepository.findByNo(no)
                .orElseThrow(() -> new BookingNotFoundException(no));

        if(!Objects.equals(memberInfo.getRoleName(), "ROLE_ADMIN")){
            checkMember(booking.getMember().getNo(), memberInfo.getNo());
        }

        boolean isVerify =  memberAdaptor.verify(memberInfo.getNo(), request);
        if(!isVerify) {
            throw new BadRequestException();
        }
        return true;
    }

    /**
     *
     * @param code 회의실 예약 시 발급 받은 예약 코드
     * @param entryTime 회의실
     * @param bookingNo 예약번호
     * @return 회의실 입실 허가 boolean 반환
     */
    @Override
    public boolean checkBooking(String code, LocalDateTime entryTime, Long bookingNo) {
        // 저장된 예약정보 찾아오기
        BookingResponse bookingResponse = bookingRepository.findByNo(bookingNo).orElseThrow(() -> new BookingNotFoundException(bookingNo));

        // 저장된 예약정보 내 예약코드
        String bookingCode = bookingResponse.getCode();

        // 예약 날짜와 회의실 입실 날짜 비교를 위한 날짜 포맷
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // 예약 날짜
        LocalDateTime bookingDateTime = bookingResponse.getStartsAt();
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
        BookingResponse.MemberInfo member = new BookingResponse.MemberInfo();
        member.setNo(booking.getMbNo());
        member.setName(mbName);

        BookingResponse.MeetingRoomInfo room = new BookingResponse.MeetingRoomInfo();
        room.setNo(booking.getRoomNo());
        room.setName(roomName);

        return new BookingResponse(
                booking.getBookingNo(),
                booking.getBookingCode(),
                booking.getBookingDate(),
                booking.getAttendeeCount(),
                booking.getFinishesAt(),
                booking.getCreatedAt(),
                booking.getBookingChange() == null ? null : booking.getBookingChange().getName(),
                member,
                room
        );
    }

    private void checkMember(Long bookingMbNo, Long loginMbNo){
        if(!Objects.equals(bookingMbNo, loginMbNo)){
            throw new ForbiddenException();
        }
    }

    private MeetingRoomResponse getMeetingRoom(Long roomNo){
        return meetingRoomAdaptor.getMeetingRoom(roomNo);
    }

    private MemberResponse getMember(Long mbNo) {
        return memberAdaptor.getMemberByMbNo(mbNo);
    }
}
