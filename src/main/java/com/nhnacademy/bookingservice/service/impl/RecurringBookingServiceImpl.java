//package com.nhnacademy.bookingservice.service.impl;
//
//import com.nhnacademy.bookingservice.common.adaptor.MeetingRoomAdaptor;
//import com.nhnacademy.bookingservice.domain.Booking;
//import com.nhnacademy.bookingservice.dto.BookingRegisterResponse;
//import com.nhnacademy.bookingservice.dto.MeetingRoomResponse;
//import com.nhnacademy.bookingservice.dto.RecurringBookingRegisterRequest;
//import com.nhnacademy.bookingservice.repository.BookingRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.DayOfWeek;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.LocalTime;
//import java.util.*;
//
//@Service
//@Transactional
//@RequiredArgsConstructor
//public class RecurringBookingServiceImpl {
//
//    private final MeetingRoomAdaptor meetingRoomAdaptor;
//
//    private final BookingRepository bookingRepository;
//
//    public BookingRegisterResponse register(RecurringBookingRegisterRequest request){
//        MeetingRoomResponse room;
//        if(request.getRoomNo() != null){
//            room = meetingRoomAdaptor.getMeetingRoom(request.getRoomNo());
//        } else {
//            room = recommendRoom(request.getAttendeeCount(), request.getStartDate(), request.getEndDate()).get();
//        }
//
//        List<LocalDateTime> dates = generateDates(
//                request.getStartDate(), request.getEndDate(), request.getDayOfWeek(), request.getStartTime()
//        );
//
//        List<Booking> bookings = new ArrayList<>();
//
//        for (LocalDateTime start : dates) {
//            LocalDateTime end = start.plusMinutes(1);
//            boolean conflict = bookingRepository.hasConflict(room.getNo(), start, end);
//            if (conflict) {
//                throw new IllegalStateException("[" + start.toLocalDate() + "] 예약 충돌이 발생했습니다.");
//            }
//
//            bookings.add(new Booking(null, room, start, end, request.title()));
//        }
//
//        bookingRepository.saveAll(bookings);
//
//        return null;
//    }
//
//    private List<LocalDateTime> generateDates(LocalDate start, LocalDate end, Set<DayOfWeek> days, LocalTime time) {
//        List<LocalDateTime> result = new ArrayList<>();
//        LocalDate current = start;
//        while (!current.isAfter(end)) {
//            if (days.contains(current.getDayOfWeek())) {
//                result.add(LocalDateTime.of(current, time));
//            }
//            current = current.plusDays(1);
//        }
//        return result;
//    }
//
//    private Optional<MeetingRoomResponse> recommendRoom(int attendeeCount, LocalDate start, LocalDate end) {
//        List<MeetingRoomResponse> candidates = meetingRoomAdaptor.getMeetingRoomList();
//
//        return candidates.stream()
//                .filter(r -> r.getMeetingRoomCapacity() >= attendeeCount)
//                .filter(r -> !bookingRepository.existsRoomNoAndDate(r.getNo(), start.atStartOfDay()))
//                .sorted(Comparator.comparing(MeetingRoomResponse::getMeetingRoomCapacity))
//                .findFirst();
//    }
//}
