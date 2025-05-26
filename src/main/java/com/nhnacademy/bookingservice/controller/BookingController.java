package com.nhnacademy.bookingservice.controller;

import com.nhnacademy.bookingservice.common.adaptor.MemberAdaptor;
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
import java.util.Objects;

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
     * @see com.nhnacademy.bookingservice.common.advice.CommonAdvice 공통 예외 처리
     */
    @ModelAttribute("memberInfo")
    public MemberResponse getMemberInfo(@RequestHeader(value = "X-USER", required = false) String email){
        return  memberAdaptor.getMemberByEmail(email);
    }

    /**
     * 예약을 등록합니다.
     *
     * @param request 예약 등록 요청 정보
     * @return 201 Created 응답
     */
    @PostMapping
    public ResponseEntity<BookingRegisterResponse> registerBooking(@Validated @RequestBody BookingRegisterRequest request, @ModelAttribute("memberInfo") MemberResponse memberInfo){
        BookingRegisterResponse response = bookingService.register(request, memberInfo);
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

    /**
     * 로그인한 회원의 예약 통계 목록을 조회합니다.
     *
     * @param memberInfo 요청 헤더 "X-USER"를 통해 주입된 회원 정보
     * @return 로그인한 회원의 예약 목록
     */
    @GetMapping("/me/statistics")
    public ResponseEntity<List<BookingResponse>> getBookingsByMember(@ModelAttribute("memberInfo") MemberResponse memberInfo){
        List<BookingResponse> responses = bookingService.getMemberBookings(memberInfo);
        return ResponseEntity.ok(responses);
    }

    /**
     * 전체 회원의 예약 통계 목록을 조회합니다.
     * 관리자만 접근 가능합니다.
     *
     * @return 전체 예약 목록
     */
    @GetMapping("/statistics")
    public ResponseEntity<List<BookingResponse>> getAllBookings(){
        List<BookingResponse> responses = bookingService.getBookings();
        return ResponseEntity.ok(responses);
    }

    /**
     * 로그인한 회원의 예약 목록(페이지네이션 포함)을 조회합니다.
     *
     * @param pageable 페이징 정보
     * @param memberInfo 요청 헤더 "X-USER"를 통해 주입된 회원 정보
     * @return 로그인한 회원의 예약 목록 페이지
     */
    @GetMapping("/me")
    public ResponseEntity<Page<BookingResponse>> getBookingsByMember(@PageableDefault(size = 10) Pageable pageable, @ModelAttribute("memberInfo") MemberResponse memberInfo){
        Page<BookingResponse> responses = bookingService.getPagedMemberBookings(memberInfo, pageable);
        return ResponseEntity.ok(responses);
    }

    /**
     * 전체 회원의 예약 목록(페이지네이션 포함)을 조회합니다.
     * 관리자만 접근 가능합니다.
     *
     * @param pageable 페이징 정보
     * @param memberInfo 요청 헤더 "X-USER"를 통해 주입된 회원 정보
     * @return 전체 예약 목록 페이지
     */
    @GetMapping
    public ResponseEntity<Page<BookingResponse>> getAllBookings(@PageableDefault(size = 10) Pageable pageable, @ModelAttribute("memberInfo") MemberResponse memberInfo){
        if(!Objects.equals(memberInfo.getRoleName(), "ROLE_ADMIN")){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Page<BookingResponse> responses = bookingService.getPagedBookings(pageable);
        return ResponseEntity.ok(responses);
    }

    /**
     * 특정 회의실의 특정 날짜에 해당하는 예약 목록을 조회합니다.
     *
     * @param roomNo 회의실 번호
     * @param date 조회할 날짜 (yyyy-MM-dd 형식)
     * @return 해당 회의실의 일일 예약 목록
     */
    @GetMapping("/meeting-rooms/{roomNo}/date/{date}")
    public ResponseEntity<List<DailyBookingResponse>> getDailyBookings(@PathVariable("roomNo") Long roomNo, @PathVariable("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
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
    public ResponseEntity<BookingResponse> updateBooking(@PathVariable("no") Long no, @Validated @RequestBody BookingUpdateRequest request, @ModelAttribute("memberInfo") MemberResponse memberInfo){
        BookingResponse response = bookingService.updateBooking(no, request, memberInfo);
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
     * @param entryRequest 회의실 입실 요청 DTO
     * @return EntryResponse 회의실 입실 응답 ResponseEntity
     */
    @PostMapping("/verify")
    public ResponseEntity<EntryResponse> checkBooking(@ModelAttribute("memberInfo") MemberResponse memberInfo, @Validated @RequestBody EntryRequest entryRequest) {
        bookingService.checkBooking(memberInfo, entryRequest.getCode(), entryRequest.getEntryTime(), entryRequest.getBookingNo());


        return ResponseEntity
                .ok(new EntryResponse(
                        HttpStatus.OK.value(),
                        "입실이 완료되었습니다.",
                        entryRequest.getEntryTime(),
                        entryRequest.getBookingNo()
                ));
    }

    @PostMapping("/{no}/verify")
    public ResponseEntity<Boolean> verifyPassword(@PathVariable("no") Long no, @Validated @RequestBody ConfirmPasswordRequest request, @ModelAttribute("memberInfo") MemberResponse memberInfo) {
        Boolean valid =  bookingService.verify(no, request, memberInfo);
        return ResponseEntity.ok(valid);
    }
}