package com.nhnacademy.bookingservice.controller;

import com.nhnacademy.bookingservice.common.adaptor.MemberAdaptor;
import com.nhnacademy.bookingservice.common.auth.MemberThreadLocal;
import com.nhnacademy.bookingservice.common.exception.member.MemberNotFoundException;
import com.nhnacademy.bookingservice.dto.*;
import com.nhnacademy.bookingservice.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * {@code BookingController}는 예약(Booking) 관련 요청을 처리하는 REST 컨트롤러입니다.
 * <ul>
 * <li> 모든 요청에는 "X-USER" 헤더를 통한 사용자 인증 정보가 필요합니다.
 * <li> 예약 등록, 조회, 수정, 삭제 및 특이사항 변경 기능을 제공합니다.
 * </ul>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final MemberAdaptor memberAdaptor;
    private final BookingService bookingService;

    /**
     * 요청 헤더의 "X-USER" 값을 기반으로 사용자 정보를 조회합니다.
     * 해당 정보는 다른 핸들러 메서드에서 {@code @ModelAttribute}로 주입됩니다.
     *
     * @param email 사용자 이메일 (요청 헤더 "X-USER"로 전달)
     * @return 사용자 정보
     * @throws feign.FeignException.Unauthorized 이메일이 없을 경우
     * @throws MemberNotFoundException 사용자 조회 실패 시
     * @see com.nhnacademy.bookingservice.common.advice.CommonAdvice 공통 예외 처리
     */
    @ModelAttribute("memberInfo")
    public MemberResponse getMemberInfo(@RequestHeader("X-USER") String email){
        ResponseEntity<MemberResponse> responseEntity = memberAdaptor.getMember(email);
        if (!responseEntity.getStatusCode().is2xxSuccessful() || responseEntity.getBody() == null) {
            throw new MemberNotFoundException();
        }
        MemberThreadLocal.setMemberNoLocal(responseEntity.getBody().getNo());
        return responseEntity.getBody();
    }

    /**
     * 예약을 등록합니다.
     *
     * @param request 예약 등록 요청 정보
     * @return 201 Created 응답
     */
    @PostMapping
    public ResponseEntity<BookingRegisterResponse> registerBooking(@Validated @RequestBody BookingRegisterRequest request){
        BookingRegisterResponse response = bookingService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 특정 예약을 조회합니다.
     *
     * @param no 예약 번호
     * @param memberInfo 사용자 정보
     * @return 예약 상세 정보
     */
    @GetMapping("/{no}")
    public ResponseEntity<BookingResponse> getBooking(@PathVariable("no") Long no, @ModelAttribute("memberInfo") MemberResponse memberInfo) {
        BookingResponse response = bookingService.getBooking(no, memberInfo);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    public ResponseEntity<Page<BookingResponse>> getBookingsByMember(@PageableDefault(size = 10) Pageable pageable, @ModelAttribute("memberInfo") MemberResponse memberInfo){
        Page<BookingResponse> responses = bookingService.getBookingsByMember(memberInfo, pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping
    public ResponseEntity<Page<BookingResponse>> getAllBookings(@PageableDefault(size = 10) Pageable pageable){
        Page<BookingResponse> responses = bookingService.getAllBookings(pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/meeting-rooms/{roomNo}/date/{date}")
    public ResponseEntity<List<DailyBookingResponse>> getDailyBookings(@PathVariable("roomNo")Long roomNo, @PathVariable("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        List<DailyBookingResponse> responses = bookingService.getDailyBookings(roomNo, date);
        return ResponseEntity.ok(responses);
    }

    /**
     * 예약 정보를 수정합니다.
     *
     * @param no 예약 번호
     * @param request 수정 요청 정보
     * @return 수정된 예약 정보
     */
    @PutMapping("/{no}")
    public ResponseEntity<BookingResponse> updateBooking(@PathVariable("no") Long no, @Validated @RequestBody BookingUpdateRequest request){
        BookingResponse response = bookingService.updateBooking(no, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 예약을 연장 합니다.
     *
     * @param bookingNo 예약 번호
     * @return 200 OK 응답
     */
    @PutMapping("/{no}/extend")
    public ResponseEntity<Void> extendBooking(@PathVariable("no") Long bookingNo) {
        bookingService.extendBooking(bookingNo);
        return ResponseEntity.ok().build();
    }

    /**
     * 예약을 종료 합니다.
     *
     * @param bookingNo 예약 번호
     * @return 200 OK 응답
     */
    @PutMapping("/{no}/finish")
    public ResponseEntity<Void> finishBooking(@PathVariable("no") Long bookingNo) {
        bookingService.finishBooking(bookingNo);
        return ResponseEntity.ok().build();
    }

    /**
     * 예약을 취소합니다.
     *
     * @param no 예약 번호
     * @param memberInfo 사용자 정보
     * @return 204 No Content 응답
     */
    @DeleteMapping("/{no}")
    public ResponseEntity<BookingResponse> deleteBooking(@PathVariable("no") Long no, @ModelAttribute("memberInfo") MemberResponse memberInfo){
        bookingService.cancelBooking(no, memberInfo);
        return ResponseEntity.noContent().build();
    }

    /**
     * 회의실 사용 예약을 확인하고, 해당 회의실 예약 정보를 반환합니다.
     *
     * @param no 예약번호
     * @param entryRequest 회의실 입실 요청 DTO
     * @return EntryResponse 회의실 입실 응답 ResponseEntity
     */
    @PostMapping("/{no}/enter")
    public ResponseEntity<EntryResponse> checkBooking(@PathVariable("no") Long no, @RequestBody EntryRequest entryRequest) {
        boolean isPermitted = bookingService.checkBooking(no, entryRequest.getCode(), entryRequest.getEntryTime(), entryRequest.getMeetingRoomNo());

        if (isPermitted) {
            return ResponseEntity
                    .ok(new EntryResponse(
                            entryRequest.getCode(),
                            entryRequest.getEntryTime(),
                            entryRequest.getMeetingRoomNo()
                    ));
        } else {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .build();
        }
    }

}