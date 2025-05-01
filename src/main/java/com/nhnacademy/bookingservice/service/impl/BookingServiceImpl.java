package com.nhnacademy.bookingservice.service.impl;

import com.nhnacademy.bookingservice.common.adaptor.MeetingRoomAdaptor;
import com.nhnacademy.bookingservice.common.adaptor.MemberAdaptor;
import com.nhnacademy.bookingservice.common.auth.MemberThreadLocal;
import com.nhnacademy.bookingservice.common.exception.ForbiddenException;
import com.nhnacademy.bookingservice.common.exception.booking.AlreadyMeetingRoomTimeException;
import com.nhnacademy.bookingservice.common.exception.booking.BookingChangeNotFoundException;
import com.nhnacademy.bookingservice.common.exception.booking.BookingNotFoundException;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;


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
    @Transactional(readOnly = true)
    public List<DailyBookingResponse> getDailyBookings(Long roomNo, LocalDate date) {
        return bookingRepository.findBookingsByDate(roomNo, date);
    }

    @Override
    @Transactional(readOnly = true)
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
        booking.updateFinishedAt(booking.getFinishedAt().plusMinutes(30));

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
        booking.updateFinishedAt(null);

        // 언제 취소 됐는지 있어야할 듯
    }

    private BookingResponse convertBookingResponse(Booking booking, String mbName, String roomName){
        return new BookingResponse(
                booking.getBookingNo(),
                booking.getBookingCode(),
                booking.getBookingDate(),
                booking.getAttendeeCount(),
                booking.getFinishedAt(),
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
