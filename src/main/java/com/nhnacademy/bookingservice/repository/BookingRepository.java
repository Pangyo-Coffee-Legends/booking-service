package com.nhnacademy.bookingservice.repository;

import com.nhnacademy.bookingservice.entity.Booking;
import com.nhnacademy.bookingservice.dto.BookingResponse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long>, CustomBookingRepository {

//    BookingResponse findById(Long no);
}
