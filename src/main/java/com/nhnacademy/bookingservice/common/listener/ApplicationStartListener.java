package com.nhnacademy.bookingservice.common.listener;

import com.nhnacademy.bookingservice.entity.Booking;
import com.nhnacademy.bookingservice.entity.BookingChange;
import com.nhnacademy.bookingservice.repository.BookingChangeRepository;
import com.nhnacademy.bookingservice.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;


@Profile("dev")
@Component
@RequiredArgsConstructor
public class ApplicationStartListener implements ApplicationListener<ApplicationReadyEvent> {

    private final BookingRepository bookingRepository;
    private final BookingChangeRepository bookingChangeRepository;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {

        BookingChange bookingChange1 = new BookingChange("연장");
        BookingChange bookingChange2 = new BookingChange("종료");
        BookingChange bookingChange3 = new BookingChange("취소");
        BookingChange bookingChange4 = new BookingChange("변경");

        bookingChangeRepository.save(bookingChange1);
        bookingChangeRepository.save(bookingChange2);
        bookingChangeRepository.save(bookingChange3);
        bookingChangeRepository.save(bookingChange4);


        Booking booking2 = Booking.ofNewBooking("test10", LocalDateTime.parse("2025-05-10T13:30:00"), 5, LocalDateTime.parse("2025-05-10T14:30:00"), 1L, null, 1L);
        Booking booking3 = Booking.ofNewBooking("test11", LocalDateTime.parse("2025-05-11T13:30:00"), 5, LocalDateTime.parse("2025-05-11T14:30:00"), 1L, null, 1L);
        Booking booking4 = Booking.ofNewBooking("test12", LocalDateTime.parse("2025-04-29T16:30:00"), 5, LocalDateTime.parse("2025-04-29T17:30:00"), 1L, null, 2L);

        bookingRepository.save(booking2);
        bookingRepository.save(booking3);
        bookingRepository.save(booking4);

        for(int i=1; i < 10; i++){
            Booking booking = Booking.ofNewBooking("test"+i, LocalDateTime.parse("2025-05-1%sT09:30:00".formatted(i)), 8, LocalDateTime.parse("2025-05-1%sT10:30:00".formatted(i)), 1L, null, 1L);
            bookingRepository.save(booking);

        }
    }
}
