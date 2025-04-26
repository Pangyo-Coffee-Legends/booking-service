package com.nhnacademy.bookingservice.service.impl;

import com.nhnacademy.bookingservice.common.adaptor.MeetingRoomAdaptor;
import com.nhnacademy.bookingservice.common.adaptor.MemberAdaptor;
import com.nhnacademy.bookingservice.common.auth.MemberThreadLocal;
import com.nhnacademy.bookingservice.common.exception.booking.BookingNotFoundException;
import com.nhnacademy.bookingservice.common.exception.meeting.MeetingRoomNotFoundException;
import com.nhnacademy.bookingservice.entity.Booking;
import com.nhnacademy.bookingservice.entity.BookingChange;
import com.nhnacademy.bookingservice.dto.*;
import com.nhnacademy.bookingservice.entity.MeetingRoom;
import com.nhnacademy.bookingservice.repository.BookingChangeRepository;
import com.nhnacademy.bookingservice.repository.BookingRepository;
import com.nhnacademy.bookingservice.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    public void save(BookingRegisterRequest request) {

        String code = UUID.randomUUID().toString().split("-")[0];

        LocalDate date = LocalDate.parse(request.getDate());
        LocalTime time = LocalTime.parse(request.getTime());
        LocalDateTime dateTime = LocalDateTime.of(date, time);
        Booking booking = Booking.ofNewBooking(code, dateTime, request.getAttendeeCount(), dateTime.plusHours(1), MemberThreadLocal.getMemberNo(), null, request.getRoomNo());

        bookingRepository.save(booking);
    }

    @Override
    public BookingResponse getBooking(Long no, MemberInfoResponse memberInfo) {

        Booking booking = bookingRepository.findById(no).orElseThrow();

        String mbName = memberInfo.getName();

        ResponseEntity<MeetingRoomResponse> roomResponseEntity = meetingRoomAdaptor.getMeetingRoom(booking.getRoomNo());
        if(roomResponseEntity == null){
            throw new MeetingRoomNotFoundException();
        }

        MeetingRoomResponse room = roomResponseEntity.getBody();
        return convertBookingResponse(booking, mbName, room.getName());

//        BookingResponse booking = bookingRepository.findByNo(no);
//
//        String mbName = memberInfo.getName();
//        booking.setMbName(mbName);
//
//
//        ResponseEntity<MeetingRoomResponse> roomResponseEntity = meetingRoomAdaptor.getMeetingRoom(booking.getRoomNo());
//        if(roomResponseEntity == null){
//            throw new MeetingRoomNotFoundException()();
//        }
//
//        MeetingRoomResponse room = roomResponseEntity.getBody();
//        booking.setRoomName(room.getName());
//
//        return booking;

    }

    @Override
    public BookingResponse updateBooking(Long no, BookingUpdateRequest request){
        Booking booking = bookingRepository.findById(no).orElseThrow(() -> new BookingNotFoundException(no));

        // todo 회원 조회, 예약회원 확인


        MeetingRoomResponse room = meetingRoomAdaptor.getMeetingRoom(booking.getRoomNo()).getBody();

        LocalDate date = LocalDate.parse(request.getDate());
        LocalTime time = LocalTime.parse(request.getTime());
        LocalDateTime dateTime = LocalDateTime.of(date, time);
        booking.update(dateTime, request.getAttendeeCount(), dateTime.plusHours(1));

        BookingChange change = bookingChangeRepository.findByName("변경");
        booking.updateBookingEvent(change);

        return convertBookingResponse(booking, null, room.getName());
    }

    @Override
    public void cancelBooking(Long no){
        Booking booking = bookingRepository.findById(no).orElseThrow(() -> new BookingNotFoundException(no));

        BookingChange change = bookingChangeRepository.findByName("취소");
        booking.updateBookingEvent(change);

        // 언제 취소 됐는지 있어야할 듯
    }

    private BookingResponse convertBookingResponse(Booking booking, String mbName, String roomName){
        return new BookingResponse(
                booking.getNo(),
                booking.getCode(),
                booking.getDate(),
                booking.getAttendees(),
                booking.getFinishedAt(),
                booking.getCreatedAt(),
                mbName,
                booking.getBookingChange() == null ? null : booking.getBookingChange().getName(),
                roomName
//                null
        );
    }
}
