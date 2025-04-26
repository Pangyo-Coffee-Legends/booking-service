package com.nhnacademy.bookingservice.service.impl;

import com.nhnacademy.bookingservice.domain.Booking;
import com.nhnacademy.bookingservice.dto.BookingRegisterRequest;
import com.nhnacademy.bookingservice.dto.BookingResponse;
import com.nhnacademy.bookingservice.repository.BookingRepository;
import com.nhnacademy.bookingservice.service.BookingService;
import lombok.RequiredArgsConstructor;
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

    @Override
    public void save(Long no,BookingRegisterRequest request) {

        String code = UUID.randomUUID().toString().split("-")[0];

        LocalDate date = LocalDate.parse(request.getDate());
        LocalTime time = LocalTime.parse(request.getTime());
        LocalDateTime dateTime = LocalDateTime.of(date, time);
        Booking booking = Booking.ofNewBooking(code, dateTime, request.getAttendeeCount(), dateTime.plusHours(1), no, null, request.getRoomNo());

        bookingRepository.save(booking);
    }

    private BookingResponse convertBookingResponse(Booking booking, String mbName){
        return new BookingResponse(
                booking.getNo(),
                booking.getCode(),
                booking.getDate(),
                booking.getAttendees(),
                booking.getFinishedAt(),
                booking.getCreatedAt(),
                mbName,
                booking.getBookingChange().getName(),
//                booking.getRoomNo()
                null
        );
    }
}
