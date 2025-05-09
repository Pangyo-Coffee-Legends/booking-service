package com.nhnacademy.bookingservice.repository;

import com.nhnacademy.bookingservice.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;


public interface BookingRepository extends JpaRepository<Booking, Long>, CustomBookingRepository {

}
