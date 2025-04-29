package com.nhnacademy.bookingservice.controller;

import com.nhnacademy.bookingservice.common.adaptor.MemberAdaptor;
import com.nhnacademy.bookingservice.common.auth.MemberThreadLocal;
import com.nhnacademy.bookingservice.common.exception.BadRequestException;
import com.nhnacademy.bookingservice.common.exception.member.MemberNotFoundException;
import com.nhnacademy.bookingservice.dto.*;
import com.nhnacademy.bookingservice.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final MemberAdaptor memberAdaptor;
    private final BookingService bookingService;

    @ModelAttribute("memberInfo")
    public MemberResponse getMemberInfo(@RequestHeader("X-USER") String email){
        if(Objects.isNull(email)){
            throw new BadRequestException();
        }

        ResponseEntity<MemberResponse> responseEntity = memberAdaptor.getMember(email);
        if (!responseEntity.getStatusCode().is2xxSuccessful() || responseEntity.getBody() == null) {
            throw new MemberNotFoundException();
        }
        MemberThreadLocal.setMemberNoLocal(responseEntity.getBody().getNo());
        return responseEntity.getBody();
    }

    @PostMapping
    public ResponseEntity<BookingResponse> registerBooking(@Validated @RequestBody BookingRegisterRequest request){
        bookingService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{no}")
    public ResponseEntity<BookingResponse> getBooking(@PathVariable("no") Long no, @ModelAttribute MemberResponse memberInfo) {
        BookingResponse response = bookingService.getBooking(no, memberInfo);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{no}")
    public ResponseEntity<BookingResponse> updateBooking(@PathVariable("no") Long no, @Validated @RequestBody BookingUpdateRequest request, @ModelAttribute MemberResponse memberInfo){
        BookingResponse response = bookingService.updateBooking(no, request, memberInfo);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{no}/changes/{change-no}")
    public ResponseEntity<Void> updateBookingChange(@PathVariable("no") Long bookingNo, @PathVariable("change-no") Long changeNo) {
        bookingService.updateBookingChange(bookingNo, changeNo);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{no}")
    public ResponseEntity<BookingResponse> deleteBooking(@PathVariable("no") Long no, @ModelAttribute MemberResponse memberInfo){
        bookingService.cancelBooking(no, memberInfo);
        return ResponseEntity.noContent().build();
    }
}
