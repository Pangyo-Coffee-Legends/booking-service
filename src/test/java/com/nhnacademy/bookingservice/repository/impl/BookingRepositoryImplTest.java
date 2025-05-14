package com.nhnacademy.bookingservice.repository.impl;

import com.nhnacademy.bookingservice.dto.BookingResponse;
import com.nhnacademy.bookingservice.dto.DailyBookingResponse;
import com.nhnacademy.bookingservice.entity.Booking;
import com.nhnacademy.bookingservice.entity.BookingChange;
import com.nhnacademy.bookingservice.repository.BookingRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
@DataJpaTest
class BookingRepositoryImplTest {

    @Autowired
    BookingRepository bookingRepository;
    @Autowired
    TestEntityManager manager;

    @BeforeEach
    void setUp(){
        manager.clear();
    }

    @Test
    @DisplayName("예약 생성")
    void save() {

        Booking booking = Booking.ofNewBooking("test4", LocalDateTime.parse("2025-04-29T09:30:00"), 5, LocalDateTime.parse("2025-04-29T17:30:00"), 1L, null, 2L);
        bookingRepository.save(booking);


        Booking find = manager.find(Booking.class, booking.getBookingNo());

        assertNotNull(find);
        assertAll(() -> {
            assertEquals("test4", find.getBookingCode());
            assertNotNull(find.getCreatedAt());
            assertNull(find.getBookingChange());
            assertEquals(1L, find.getMbNo());
            assertEquals(2L, find.getRoomNo());
        });

    }

    @Test
    @DisplayName("예약 조회 - 번호")
    void findByNo() {
        Booking booking1 = Booking.ofNewBooking("test1", LocalDateTime.parse("2025-04-29T09:30:00"), 8, LocalDateTime.parse("2025-04-29T10:30:00"), 1L, null, 1L);
        manager.persistAndFlush(booking1);

        Optional<BookingResponse> optional = bookingRepository.findByNo(1L);
        assertTrue(optional.isPresent());
        BookingResponse response = optional.get();
        assertAll(() -> {
            assertEquals("test1", response.getCode());
            assertNull(response.getChangeName());
            assertEquals(1L, response.getMbNo());
            assertEquals(1L, response.getRoomNo());
        });
    }

    @Test
    @DisplayName("예약 조회(리스트) - 사용자별")
    void findBookingsByMbNo_list() {
        Booking booking1 = Booking.ofNewBooking("test2", LocalDateTime.parse("2025-04-29T09:30:00"), 8, LocalDateTime.parse("2025-04-29T10:30:00"), 1L, null, 2L);
        Booking booking2 = Booking.ofNewBooking("test3", LocalDateTime.parse("2025-04-30T09:30:00"), 8, LocalDateTime.parse("2025-04-30T10:30:00"), 2L, null, 2L);
        manager.persist(booking1);
        manager.persist(booking2);
        manager.flush();
        manager.clear();

        List<BookingResponse> response = bookingRepository.findBookingList(1L);

        assertNotNull(response);
        assertAll(() -> {
            assertEquals(1, response.size());
            assertEquals("test2", response.getFirst().getCode());
            assertEquals(8, response.getFirst().getAttendeeCount());
            assertEquals(1L, response.getFirst().getMbNo());
            assertEquals(2L, response.getFirst().getRoomNo());
        });
    }

    @Test
    @DisplayName("예약 조회(리스트) - 전체")
    void findAllBookings_list() {
        Booking booking1 = Booking.ofNewBooking("test2", LocalDateTime.parse("2025-04-29T09:30:00"), 8, LocalDateTime.parse("2025-04-29T10:30:00"), 1L, null, 2L);
        Booking booking2 = Booking.ofNewBooking("test3", LocalDateTime.parse("2025-04-30T09:30:00"), 9, LocalDateTime.parse("2025-04-30T10:30:00"), 2L, null, 2L);
        manager.persist(booking1);
        manager.persist(booking2);
        manager.flush();
        manager.clear();

        List<BookingResponse> response = bookingRepository.findBookingList(null);

        assertNotNull(response);
        assertAll(() -> {
            assertEquals(2, response.size());

            assertEquals("test3", response.get(1).getCode());
            assertEquals(9, response.get(1).getAttendeeCount());
            assertEquals(2L, response.get(1).getMbNo());
            assertEquals(2L, response.get(1).getRoomNo());

            assertEquals("test2", response.getFirst().getCode());
            assertEquals(8, response.getFirst().getAttendeeCount());
            assertEquals(1L, response.getFirst().getMbNo());
            assertEquals(2L, response.getFirst().getRoomNo());
        });
    }

    @Test
    @DisplayName("예약 조회(페이징) - 사용자별")
    void findBookingsByMbNo_page() {
        Booking booking = Booking.ofNewBooking("test2", LocalDateTime.parse("2025-04-29T09:30:00"), 8, LocalDateTime.parse("2025-04-29T10:30:00"), 1L, null, 2L);
        manager.persistAndFlush(booking);

        BookingResponse bookingResponse = new BookingResponse(booking.getBookingNo(), booking.getBookingCode(), booking.getBookingDate(), booking.getAttendeeCount(), booking.getFinishesAt(), booking.getCreatedAt(), booking.getMbNo(),  null, booking.getRoomNo());

        Page<BookingResponse> response = bookingRepository.findBookings(1L, Pageable.ofSize(1));

        assertNotNull(response);
        assertTrue(response.getContent().contains(bookingResponse));
    }

    @Test
    @DisplayName("예약 조회(페이징) - 전체")
    void findAllBookings_page() {
        Booking booking1 = Booking.ofNewBooking("test2", LocalDateTime.parse("2025-04-29T09:30:00"), 8, LocalDateTime.parse("2025-04-29T10:30:00"), 1L, null, 2L);
        Booking booking2 = Booking.ofNewBooking("test3", LocalDateTime.parse("2025-04-29T10:30:00"), 8, LocalDateTime.parse("2025-04-29T11:30:00"), 1L, null, 2L);
        Booking booking3 = Booking.ofNewBooking("test4", LocalDateTime.parse("2025-04-30T12:30:00"), 8, LocalDateTime.parse("2025-04-30T13:30:00"), 1L, null, 2L);

        manager.persist(booking1);
        manager.persist(booking2);
        manager.persist(booking3);
        manager.flush();
        manager.clear();

        Page<BookingResponse> response = bookingRepository.findBookings(null, PageRequest.of(1, 2));

        assertNotNull(response);
        assertAll(() -> {
            assertEquals(2, response.getTotalPages());
            assertEquals(3, response.getTotalElements());
        });

        BookingResponse bookingResponse = response.getContent().getFirst();

        assertNotNull(bookingResponse);
        assertAll(() -> {
            assertEquals("test4", bookingResponse.getCode());
            assertEquals(1L, bookingResponse.getMbNo());
            assertEquals(2L, bookingResponse.getRoomNo());
        });
    }

    @Test
    @DisplayName("예약 조회(페이징) - 정렬")
    void findAllBookings_sort() {
        Booking booking1 = Booking.ofNewBooking("test2", LocalDateTime.parse("2025-04-29T09:30:00"), 8, LocalDateTime.parse("2025-04-29T10:30:00"), 1L, null, 2L);
        Booking booking2 = Booking.ofNewBooking("test3", LocalDateTime.parse("2025-04-29T10:30:00"), 8, LocalDateTime.parse("2025-04-29T11:30:00"), 1L, null, 2L);
        Booking booking3 = Booking.ofNewBooking("test4", LocalDateTime.parse("2025-04-30T12:30:00"), 8, LocalDateTime.parse("2025-04-30T13:30:00"), 1L, null, 2L);

        manager.persist(booking1);
        manager.persist(booking2);
        manager.persist(booking3);
        manager.flush();
        manager.clear();

        Sort sort = Sort.by(Sort.Order.desc("createdAt"));

        Page<BookingResponse> response = bookingRepository.findBookings(null, PageRequest.of(1, 2, sort));

        assertNotNull(response);
        assertAll(() -> {
            assertEquals(2, response.getTotalPages());
            assertEquals(3, response.getTotalElements());
        });

        BookingResponse bookingResponse = response.getContent().getFirst();

        assertNotNull(bookingResponse);
        assertAll(() -> {
            assertEquals("test2", bookingResponse.getCode());
            assertEquals(1L, bookingResponse.getMbNo());
            assertEquals(2L, bookingResponse.getRoomNo());
        });
    }

    @Test
    @DisplayName("예약 조회 - 날짜별")
    void findBookingsByDate() {
        BookingChange bookingChange1 = new BookingChange("연장");
        BookingChange bookingChange2 = new BookingChange("종료");
        BookingChange bookingChange3 = new BookingChange("취소");
        manager.persist(bookingChange1);
        manager.persist(bookingChange2);
        manager.persist(bookingChange3);
        manager.flush();
        manager.clear();

        Booking booking1 = Booking.ofNewBooking("test2", LocalDateTime.parse("2025-04-29T09:30:00"), 8, LocalDateTime.parse("2025-04-29T10:30:00"), 1L, null, 2L);
        Booking booking2 = Booking.ofNewBooking("test3", LocalDateTime.parse("2025-04-29T10:30:00"), 8, LocalDateTime.parse("2025-04-29T11:30:00"), 1L, null, 2L);
        Booking booking3 = Booking.ofNewBooking("test4", LocalDateTime.parse("2025-04-29T12:30:00"), 8, LocalDateTime.parse("2025-04-29T13:30:00"), 1L, bookingChange3, 2L);

        manager.persist(booking1);
        manager.persist(booking2);
        manager.persist(booking3);
        manager.flush();
        manager.clear();

        List<DailyBookingResponse> response = bookingRepository.findBookingsByDate(2L, LocalDate.parse("2025-04-29"));

        log.debug("response: {}", response);
        assertNotNull(response);

        assertAll(() -> {
            assertEquals(2, response.size());
            assertEquals(1L, response.getFirst().getNo());
            assertEquals(LocalDateTime.parse("2025-04-29T09:30:00"), response.getFirst().getStartsAt());
            assertEquals(LocalDateTime.parse("2025-04-29T10:30:00"), response.getFirst().getFinishesAt());
            assertEquals(2L, response.get(1).getNo());
            assertEquals(LocalDateTime.parse("2025-04-29T10:30:00"), response.get(1).getStartsAt());
            assertEquals(LocalDateTime.parse("2025-04-29T11:30:00"), response.get(1).getFinishesAt());
        });
    }

    @ParameterizedTest
    @MethodSource("provideBookingAndDate")
    @DisplayName("예약 중복 체크 - 겹치는 시간에 예약하는 경우")
    void existsRoomNoAndDate_true(Booking booking, LocalDateTime targetDate) {
        manager.persistAndFlush(booking);

        boolean actual = bookingRepository.existsRoomNoAndDate(2L, targetDate);

        assertTrue(actual);
    }

    private static Stream<Arguments> provideBookingAndDate() {
        return Stream.of(
                Arguments.of(
                        Booking.ofNewBooking("test3", LocalDateTime.parse("2025-04-29T09:30:00"), 8,
                                LocalDateTime.parse("2025-04-29T10:30:00"), 1L, null, 2L),
                        LocalDateTime.parse("2025-04-29T09:30:00")
                ),
                Arguments.of(
                        Booking.ofNewBooking("test3", LocalDateTime.parse("2025-04-29T09:30:00"), 8,
                                LocalDateTime.parse("2025-04-29T10:30:00"), 1L, null, 2L),
                        LocalDateTime.parse("2025-04-29T10:00:00")
                ),
                Arguments.of(
                        Booking.ofNewBooking("test3", LocalDateTime.parse("2025-04-29T10:30:00"), 8,
                                LocalDateTime.parse("2025-04-29T10:30:00"), 1L, null, 2L),
                        LocalDateTime.parse("2025-04-29T10:00:00")
                )
        );
    }



    @Test
    @DisplayName("예약 중복 체크 - False")
    void existsRoomNoAndDate_false() {
        Booking booking = Booking.ofNewBooking("test3", LocalDateTime.parse("2025-04-29T09:30:00"), 8, LocalDateTime.parse("2025-04-29T10:30:00"), 1L, null, 2L);
        manager.persistAndFlush(booking);

        boolean actual = bookingRepository.existsRoomNoAndDate(2L, LocalDateTime.parse("2025-04-29T10:30:00"));

        assertFalse(actual);
    }

    @Test
    @DisplayName("예약 중복 체크 - True")
    void hasBookingStartingAt_true() {
        Booking booking = Booking.ofNewBooking("test3", LocalDateTime.parse("2025-04-29T09:30:00"), 8, LocalDateTime.parse("2025-04-29T10:30:00"), 1L, null, 2L);
        manager.persistAndFlush(booking);

        boolean actual = bookingRepository.hasBookingStartingAt(2L, LocalDateTime.parse("2025-04-29T09:30:00"));

        assertTrue(actual);
    }

    @Test
    @DisplayName("예약 중복 체크 - False")
    void hasBookingStartingAt_false() {
        Booking booking = Booking.ofNewBooking("test3", LocalDateTime.parse("2025-04-29T09:30:00"), 8, LocalDateTime.parse("2025-04-29T10:30:00"), 1L, null, 2L);
        manager.persistAndFlush(booking);

        boolean actual = bookingRepository.hasBookingStartingAt(2L, LocalDateTime.parse("2025-04-29T10:30:00"));

        assertFalse(actual);
    }
}