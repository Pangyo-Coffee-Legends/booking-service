package com.nhnacademy.bookingservice.common.event;

import com.nhnacademy.bookingservice.common.adaptor.MeetingRoomAdaptor;
import com.nhnacademy.bookingservice.common.adaptor.NotifyAdaptor;
import com.nhnacademy.bookingservice.common.exception.booking.BookingNotFoundException;
import com.nhnacademy.bookingservice.dto.BookingResponse;
import com.nhnacademy.bookingservice.dto.MeetingRoomResponse;
import com.nhnacademy.bookingservice.dto.RemindRequest;
import com.nhnacademy.bookingservice.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;

import java.time.format.DateTimeFormatter;

@RequiredArgsConstructor
public class BookingReminderEventListener{

    private final NotifyAdaptor notifyAdaptor;
    private final MeetingRoomAdaptor meetingRoomAdaptor;
    private final BookingRepository bookingRepository;

    @Async
    public void sendReminder(BookingReminderEvent event) {
        BookingResponse booking = bookingRepository.findByNo(event.getBookingNo()).orElseThrow(BookingNotFoundException::new);

        MeetingRoomResponse room = getMeetingRoom(booking.getRoom().getNo());
        booking.getRoom().setName(room.getMeetingRoomName());

        RemindRequest request = new RemindRequest(
                event.getEmail(),
                "회의 10분 전 알림",
                "%s에 %s에서 회의가 시작됩니다. 입실해 주세요.".formatted(
                        booking.getStartsAt().format(DateTimeFormatter.ofPattern("HH:mm")),
                        room.getMeetingRoomName()),
                "ROLE_ALL");

        notifyAdaptor.sendRemindText(request);

    }

    private MeetingRoomResponse getMeetingRoom(Long roomNo){
        return meetingRoomAdaptor.getMeetingRoom(roomNo);
    }

}
