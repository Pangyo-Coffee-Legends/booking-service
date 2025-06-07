package com.nhnacademy.bookingservice.repository;

import com.nhnacademy.bookingservice.dto.BookingResponse;
import com.nhnacademy.bookingservice.dto.DailyBookingResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
    Optional<BookingResponse> findByNo(Long no);

    /**
     * 예약 목록을 조회합니다.
     * <p>
     * 예약자 번호가 {@code null}인 경우 전체 예약 목록을 조회하며,<br>
     * 그렇지 않으면 해당 예약자의 예약 목록만 조회합니다.
     *
     * @param attendeeNo 예약자 식별번호 (nullable)
     * @return 예약 목록이 포함된 List 객체
     */
    List<BookingResponse> findBookingList(Long attendeeNo);

    /**
     * 페이징(Pageable) 조건에 맞는 예약 목록을 조회합니다.
     * <p>
     * 예약자 번호가 {@code null}인 경우 전체 예약 목록을 조회하며,<br>
     * 그렇지 않으면 해당 예약자의 예약 목록만 조회합니다.
     *
     * @param attendeeNo 예약자 식별번호 (nullable)
     * @param pageable 페이징 및 정렬 정보
     * @return 예약 목록이 포함된 Page 객체
     */
    Page<BookingResponse> findBookings(Long attendeeNo, Pageable pageable);

    /**
     * 회의실 번호(roomNo)와 날짜(date)를 기반으로 예약 정보를 조회합니다.
     *
     * @param roomNo 선택한 회의실 번호
     * @param date 선택한 예약 날짜
     * @return 조회된 예약 정보 (DailyBookingResponse)
     */
    List<DailyBookingResponse> findBookingsByDate(Long roomNo, LocalDate date);

    /**
     * 주어진 시각에 해당 회의실에 겹치는 예약이 존재하는지 확인합니다.
     * 예약 시작 시각과 종료 시각을 기준으로, 전달된 시각(date)이 이 사이에 포함되는 경우 예약이 존재한다고 판단합니다.
     *
     * @param roomNo 회의실 번호
     * @param startsAt   예약 확인 기준 시작 시각
     * @param finishesAt 예약 확인 기준 종료 시각
     * @return 겹치는 예약이 존재하면 true, 없으면 false
     */
    boolean existsOverlappingBooking(Long roomNo, LocalDateTime startsAt, LocalDateTime finishesAt);

    /**
     * 주어진 시각에 해당 회의실에서 정확히 그 시각에 시작하는 예약이 존재하는지 확인합니다.
     *
     * @param roomNo 회의실 번호
     * @param date   예약 시작 시각
     * @return 정확히 그 시각에 시작하는 예약이 존재하면 true, 없으면 false
     */
    boolean hasBookingStartingAt(Long roomNo, LocalDateTime date);
}