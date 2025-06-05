package com.nhnacademy.bookingservice.repository;

import com.nhnacademy.bookingservice.domain.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface BookingRepository extends JpaRepository<Booking, Long>, CustomBookingRepository {

    Optional<Booking> findBookingByBookingNo(Long bookingNo);
}
