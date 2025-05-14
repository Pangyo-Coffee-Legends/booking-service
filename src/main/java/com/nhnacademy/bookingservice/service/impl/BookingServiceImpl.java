package com.nhnacademy.bookingservice.service.impl;

import com.nhnacademy.bookingservice.common.adaptor.MeetingRoomAdaptor;
import com.nhnacademy.bookingservice.common.adaptor.MemberAdaptor;
import com.nhnacademy.bookingservice.common.event.BookingCancelEvent;
import com.nhnacademy.bookingservice.common.event.BookingCreatedEvent;
import com.nhnacademy.bookingservice.common.exception.BadRequestException;
import com.nhnacademy.bookingservice.common.exception.ForbiddenException;
import com.nhnacademy.bookingservice.common.exception.booking.AlreadyMeetingRoomTimeException;
import com.nhnacademy.bookingservice.common.exception.booking.BookingChangeNotFoundException;
import com.nhnacademy.bookingservice.common.exception.booking.BookingNotFoundException;
import com.nhnacademy.bookingservice.common.exception.meeting.MeetingRoomCapacityExceededException;
import com.nhnacademy.bookingservice.entity.Booking;
import com.nhnacademy.bookingservice.entity.BookingChange;
import com.nhnacademy.bookingservice.dto.*;
import com.nhnacademy.bookingservice.entity.BookingChangeType;
import com.nhnacademy.bookingservice.repository.BookingChangeRepository;
import com.nhnacademy.bookingservice.repository.BookingRepository;
import com.nhnacademy.bookingservice.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
        booking.setMbName(memberInfo.getName());

        checkMember(booking.getMbNo(), memberInfo.getNo());

        MeetingRoomResponse room = getMeetingRoom(booking.getRoomNo());
        booking.setRoomName(room.getMeetingRoomName());

        return booking;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getMemberBookings(MemberResponse memberInfo) {
        List<BookingResponse> bookings = bookingRepository.findBookingList(memberInfo.getNo());

        bookings.forEach(booking -> {
            booking.setMbName(memberInfo.getName());

            MeetingRoomResponse room = getMeetingRoom(booking.getRoomNo());
            booking.setRoomName(room.getMeetingRoomName());
        });
        return bookings;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getBookings() {
        List<BookingResponse> bookings = bookingRepository.findBookingList(null);

        bookings.forEach(booking -> {
            MemberResponse member = getMember(booking.getMbNo());
            booking.setMbName(member.getName());
            booking.setEmail(member.getEmail());

            MeetingRoomResponse room = getMeetingRoom(booking.getRoomNo());
            booking.setRoomName(room.getMeetingRoomName());
        });
        return bookings;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> getPagedMemberBookings(MemberResponse memberInfo, Pageable pageable) {
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
    public Page<BookingResponse> getPagedBookings(Pageable pageable) {

        Page<BookingResponse> bookings = bookingRepository.findBookings(null, pageable);

        bookings.forEach(booking -> {
            MemberResponse member = getMember(booking.getMbNo());
            booking.setMbName(member.getName());
            booking.setEmail(member.getEmail());

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

        if(bookingRepository.existsRoomNoAndDate(booking.getRoomNo(), booking.getFinishedAt())){
            throw new AlreadyMeetingRoomTimeException();
        }

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

        if(!Objects.equals(memberInfo.getRoleName(), "ROLE_ADMIN")){
            checkMember(booking.getMbNo(), memberInfo.getNo());
        }
        BookingChange change = bookingChangeRepository.findById(BookingChangeType.CANCEL.getId())
                .orElseThrow(() -> new BookingChangeNotFoundException(BookingChangeType.CANCEL.getId()));

        booking.updateBookingEvent(change);
        booking.updateFinishedAt(null);

        publisher.publishEvent(new BookingCancelEvent(this, memberInfo.getEmail(), booking.getBookingNo()));
    }

    @Override
    public boolean verify(Long no, ConfirmPasswordRequest request, MemberResponse memberInfo) {
        BookingResponse booking = bookingRepository.findByNo(no)
                                                    .orElseThrow(() -> new BookingNotFoundException(no));

        if(!Objects.equals(memberInfo.getRoleName(), "ROLE_ADMIN")){
            checkMember(booking.getMbNo(), memberInfo.getNo());
        }

       boolean isVerify =  memberAdaptor.verify(memberInfo.getNo(), request);
        if(!isVerify) {
            throw new BadRequestException();
        }
        return true;
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
                null,
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
        return meetingRoomAdaptor.getMeetingRoom(roomNo);
    }

    private MemberResponse getMember(Long mbNo) {
        return memberAdaptor.getMember(mbNo);
    }
}
