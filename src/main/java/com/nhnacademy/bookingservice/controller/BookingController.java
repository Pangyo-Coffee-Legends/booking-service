package com.nhnacademy.bookingservice.controller;

import com.nhnacademy.bookingservice.adaptor.MemberAdaptor;
import com.nhnacademy.bookingservice.dto.BookingRegisterRequest;
import com.nhnacademy.bookingservice.dto.BookingResponse;
import com.nhnacademy.bookingservice.dto.MemberInfoResponse;
import com.nhnacademy.bookingservice.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
public class BookingController {

    private final MemberAdaptor memberAdaptor;
    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponse> registerBooking(@RequestHeader("X-USER")String email, @RequestBody BookingRegisterRequest request){
        ResponseEntity<MemberInfoResponse> responseEntity = memberAdaptor.getMember(email);
        if(responseEntity == null){
            return null;
        }

        bookingService.save(responseEntity.getBody().getNo(), request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
