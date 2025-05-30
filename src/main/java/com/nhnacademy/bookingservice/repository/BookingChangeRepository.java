package com.nhnacademy.bookingservice.repository;

import com.nhnacademy.bookingservice.domain.BookingChange;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingChangeRepository extends JpaRepository<BookingChange, Long> {
    BookingChange findByName(String name);
}
