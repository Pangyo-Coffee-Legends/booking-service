package com.nhnacademy.bookingservice.service;

import com.nhnacademy.bookingservice.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 예약(Booking) 관련 비즈니스 로직을 정의하는 인터페이스입니다.
 * <p>주요 기능:
 * <ul>
 *   <li>예약 저장</li>
 *   <li>페이징 조건에 따른 예약 정보 조회</li>
 *   <li>예약 정보 수정</li>
 *   <li>예약 특이사항(종료, 연장, 취소 등) 업데이트</li>
 *   <li>예약을 취소</li>
 * </ul>
 */
public interface BookingService {

    /**
     * 새로운 예약을 저장합니다.
     *
     * @param bookingRegisterRequest 예약 등록 요청 정보를 담은 객체
     */
    BookingRegisterResponse register(BookingRegisterRequest bookingRegisterRequest);

    /**
     * 예약 번호와 회원 정보를 기반으로 예약 정보를 조회합니다.
     *
     * @param no 예약 번호
     * @param memberInfo 회원 정보
     * @return 조회된 예약 정보
     */
    BookingResponse getBooking(Long no, MemberResponse memberInfo);

    /**
     * 예약 목록을 페이징 처리하여 조회합니다.
     *
     * @param memberResponse 예약한 사용자 정보
     * @param pageable 페이지 번호, 크기, 정렬 정보를 담고 있는 {@link Pageable} 객체
     * @return 조회된 예약 정보 {@link BookingResponse}를 포함하는 {@link List} 객체
     */
    Page<BookingResponse> getBookingsByMember(MemberResponse memberResponse, Pageable pageable);

    /**
     * 예약 전체 목록을 페이징 처리하여 조회합니다.
     *
     * @param pageable 페이지 번호, 크기, 정렬 정보를 담고 있는 {@link Pageable} 객체
     * @return 조회된 예약 정보 {@link BookingResponse}를 포함하는 {@link List} 객체
     */
    Page<BookingResponse> getAllBookings(Pageable pageable);

    List<DailyBookingResponse> getDailyBookings(Long roomNo, LocalDate date);
    /**
     * 예약 정보를 수정합니다.
     *
     * @param no 예약 번호
     * @param request 예약 수정 요청 정보를 담은 객체
     * @return 수정된 예약 정보
     */
    BookingResponse updateBooking(Long no, BookingUpdateRequest request);

    /**
     * 예약을 연장 합니다.
     *
     * @param bookingNo 예약 번호
     */
    void extendBooking(Long bookingNo);

    /**
     * 예약을 종료 합니다.
     *
     * @param bookingNo 예약 번호
     */
    void finishBooking(Long bookingNo);

    /**
     * 예약을 취소합니다.
     *
     * @param no 예약 번호
     */
    void cancelBooking(Long no, MemberResponse memberInfo);

    boolean checkBooking(Long no, String code, LocalDateTime entryTime, Long meetingRoomNo);
}

