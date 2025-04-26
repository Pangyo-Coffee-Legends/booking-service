package com.nhnacademy.bookingservice.controller;

import com.nhnacademy.bookingservice.common.adaptor.MemberAdaptor;
import com.nhnacademy.bookingservice.common.auth.MemberThreadLocal;
import com.nhnacademy.bookingservice.dto.BookingRegisterRequest;
import com.nhnacademy.bookingservice.dto.BookingResponse;
import com.nhnacademy.bookingservice.dto.BookingUpdateRequest;
import com.nhnacademy.bookingservice.dto.MemberInfoResponse;
import com.nhnacademy.bookingservice.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final MemberAdaptor memberAdaptor;
    private final BookingService bookingService;

    @ModelAttribute("memberInfo")
    public MemberInfoResponse getMemberInfo(@RequestHeader("X-USER")String email){
        ResponseEntity<MemberInfoResponse> responseEntity = memberAdaptor.getMember(email);
        MemberThreadLocal.setMemberNoLocal(responseEntity.getBody().getNo());

        return responseEntity.getBody();
    }

    @PostMapping
    public ResponseEntity<BookingResponse> registerBooking(@RequestBody BookingRegisterRequest request){
        bookingService.save(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{no}")
    public ResponseEntity<BookingResponse> getBooking(@PathVariable("no") Long no, @ModelAttribute("memberInfo") MemberInfoResponse memberInfo) {
        BookingResponse response = bookingService.getBooking(no, memberInfo);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{no}")
    public ResponseEntity<BookingResponse> updateBooking(@PathVariable("no") Long no, BookingUpdateRequest request){
        BookingResponse response = bookingService.updateBooking(no, request);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{no}")
    public ResponseEntity<BookingResponse> deleteBooking(@PathVariable("no") Long no){
        bookingService.cancelBooking(no);
        return ResponseEntity.noContent().build();
    }
}
