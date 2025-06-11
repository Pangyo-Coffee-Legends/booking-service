package com.nhnacademy.bookingservice.common.schedule;


import com.nhnacademy.bookingservice.common.adaptor.MemberAdaptor;
import com.nhnacademy.bookingservice.common.event.BookingReminderEvent;
import com.nhnacademy.bookingservice.dto.BookingResponse;
import com.nhnacademy.bookingservice.dto.DailyBookingResponse;
import com.nhnacademy.bookingservice.dto.MemberResponse;
import com.nhnacademy.bookingservice.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingReminderScheduler {

    private final BookingRepository bookingRepository;
    private final MemberAdaptor memberAdaptor;
    private final ApplicationEventPublisher publisher;

    @Scheduled(cron = "0 0/50 8-17 * * *")
    public void sendUpcomingBookingReminders() {
        log.info("🔔 [Scheduler] 예약 10분 전 알림 발송 시작");

        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        LocalDateTime targetTime = now.plusMinutes(10);

        List<BookingResponse> reminders = bookingRepository.findBookingsToRemind(targetTime);

        for (BookingResponse reminder : reminders) {
            try {
                MemberResponse member = memberAdaptor.getMemberByMbNo(reminder.getMember().getNo());
                publisher.publishEvent(new BookingReminderEvent(this, member.getEmail(), reminder.getNo()));

                log.info("알림 전송 완료 - 예약번호: {}", reminder.getNo());
            } catch (Exception e) {
                log.error("알림 전송 실패 - 예약번호: {}, 이유: {}", reminder.getNo(), e.getMessage());
            }
        }

        log.info("🔔 [Scheduler] 예약 10분 전 알림 발송 종료 (총 {}건)", reminders.size());
    }
}
