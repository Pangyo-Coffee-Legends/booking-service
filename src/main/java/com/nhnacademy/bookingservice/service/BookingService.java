package com.nhnacademy.bookingservice.service;

import com.nhnacademy.bookingservice.dto.BookingRegisterRequest;

public interface BookingService {

    void save (Long no, BookingRegisterRequest request);


}
