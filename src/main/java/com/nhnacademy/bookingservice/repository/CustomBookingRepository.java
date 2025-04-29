package com.nhnacademy.bookingservice.repository;

import com.nhnacademy.bookingservice.dto.BookingResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

/**
 * Booking 엔티티에 대한 커스텀 조회 기능을 정의하는 인터페이스입니다.
 *
 * <p>주요 기능:
 * <ul>
 *   <li>예약 번호(no)로 예약 단건 조회</li>
 *   <li>페이징 조건에 따른 예약 목록 조회</li>
 *   <li>특정 회의실 번호(roomNo)와 날짜(date)에 대한 예약 존재 여부 확인</li>
 * </ul>
 */
public interface CustomBookingRepository {

    /**
     * 예약 번호(no)를 기반으로 예약 정보를 조회합니다. (단순 조회만 할 경우 사용)
     *
     * @param no 예약 번호
     * @return 조회된 예약 정보 (BookingResponse)
     */
    BookingResponse findByNo(Long no);

    /**
     * 페이징(Pageable) 조건에 맞는 예약 목록을 조회합니다.
     *
     * @param pageable 페이징 및 정렬 정보
     * @return 예약 목록이 포함된 Page 객체
     */
    Page<BookingResponse> findBookings(Pageable pageable);

    /**
     * 주어진 회의실 번호와 예약 일자에 예약이 존재하는지 여부를 확인합니다.
     *
     * @param roomNo 회의실 번호
     * @param date 예약 일자 및 시간
     * @return 예약이 존재하면 true, 존재하지 않으면 false
     */
    boolean existsRoomNoAndDate(Long roomNo, LocalDateTime date);
}