package com.nhnacademy.bookingservice.repository;

import com.nhnacademy.bookingservice.dto.BookingResponse;

public interface CustomBookingRepository {

    BookingResponse findByNo(Long no);

}
