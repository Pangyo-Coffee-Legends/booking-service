package com.nhnacademy.bookingservice.service;

import com.nhnacademy.bookingservice.dto.BookingRegisterRequest;
import com.nhnacademy.bookingservice.dto.BookingResponse;
import com.nhnacademy.bookingservice.dto.BookingUpdateRequest;
import com.nhnacademy.bookingservice.dto.MemberInfoResponse;

public interface BookingService {

    void save (BookingRegisterRequest request);

    BookingResponse getBooking(Long no, MemberInfoResponse memberInfo);

    BookingResponse updateBooking(Long no, BookingUpdateRequest request);

    void cancelBooking(Long no);

}
