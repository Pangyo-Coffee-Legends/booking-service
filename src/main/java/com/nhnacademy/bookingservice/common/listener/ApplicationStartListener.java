package com.nhnacademy.bookingservice.common.listener;

import com.nhnacademy.bookingservice.domain.BookingChange;
import com.nhnacademy.bookingservice.dto.BookingRegisterRequest;
import com.nhnacademy.bookingservice.dto.MemberResponse;
import com.nhnacademy.bookingservice.repository.BookingChangeRepository;
import com.nhnacademy.bookingservice.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;


@Profile("dev")
@Component
@RequiredArgsConstructor
public class ApplicationStartListener implements ApplicationListener<ApplicationReadyEvent> {

    private final BookingChangeRepository bookingChangeRepository;
    private final BookingService bookingService;

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

        BookingRegisterRequest request1 = new BookingRegisterRequest(1L, "2025-06-02", "16:00", 5);

        MemberResponse member = new MemberResponse(1L, "Noah", "test@test.com", "010-1111-2222", "ROLE_ADMIN");

        bookingService.register(request1, member);
    }
}
