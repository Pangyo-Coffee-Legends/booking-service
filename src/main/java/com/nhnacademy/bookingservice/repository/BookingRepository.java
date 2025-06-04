package com.nhnacademy.bookingservice.repository;

import com.nhnacademy.bookingservice.domain.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface BookingRepository extends JpaRepository<Booking, Long>, CustomBookingRepository {

    List<Booking> findBookingByBookingNo(Long bookingNo);
}
