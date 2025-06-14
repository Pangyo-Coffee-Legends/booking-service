package com.nhnacademy.bookingservice.common.event;

import com.nhnacademy.bookingservice.common.adaptor.MeetingRoomAdaptor;
import com.nhnacademy.bookingservice.common.adaptor.NotifyAdaptor;
import com.nhnacademy.bookingservice.common.exception.booking.BookingNotFoundException;
import com.nhnacademy.bookingservice.dto.BookingResponse;
import com.nhnacademy.bookingservice.dto.EmailRequest;
import com.nhnacademy.bookingservice.dto.MeetingRoomResponse;
import com.nhnacademy.bookingservice.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class BookingEmailEventListener {

    private final NotifyAdaptor notifyAdaptor;
    private final MeetingRoomAdaptor meetingRoomAdaptor;
    private final BookingRepository bookingRepository;
    private static final String ROLE_TYPE = "ROLE_ADMIN";

    @Async
    @TransactionalEventListener(classes = BookingCreatedEvent.class, phase = TransactionPhase.AFTER_COMMIT)
    public void handleBookingCreatedEvent(BookingCreatedEvent event){

        BookingResponse booking = bookingRepository.findByNo(event.getBookingNo()).orElseThrow(BookingNotFoundException::new);

        MeetingRoomResponse room = getMeetingRoom(booking.getRoom().getNo());
        booking.getRoom().setName(room.getMeetingRoomName());

        EmailRequest request = new EmailRequest(
                event.getEmail(),
                "[예약 완료] 회의실 예약이 확인되었습니다",
                """
                        <h2>회의실 예약이 완료되었습니다</h2>
                           <p><strong>%s</strong>님, 요청하신 회의실 예약이 아래와 같이 정상적으로 완료되었습니다.</p>
                            <br>
                       
                           <hr>
                            <p>
                                <strong>예약 번호:</strong> %s<br>
                                <strong>회의실:</strong> %s<br>
                                <strong>날짜:</strong> %s<br>
                            </p>
                            <hr>
                            <br>
                        
                           <p>
                                회의 시작 <strong>10분 후</strong>까지 입장 확인이 되지 않을 경우, 해당 예약은 자동으로 취소됩니다.<br>
                                예약 변경이나 취소가 필요하신 경우, 아래 버튼을 눌러 주세요.
                           </p>
                        
                           <div class="cta">
                               <a href="https://aiot2.live/booking/history">예약 확인/변경하기</a>
                           </div>
                            <br>
                        
                           <p style="color: gray; font-size: small;">
                                본 메일은 시스템에 의해 자동 발송되었습니다.<br>
                                문의사항이 있으시면 <a href="mailto:help@aiot2.live">help@aiot2.live</a> 으로 연락 주세요.
                           </p>
                        """.formatted(
                                event.getEmail(),
                                booking.getCode(),
                                booking.getRoom().getName(),
                                booking.getStartsAt().toString().replace("T", " ")),
                ROLE_TYPE
        );

        notifyAdaptor.sendHtmlEmail(request);
    }

    @Async
    @TransactionalEventListener(classes = BookingChangeEvent.class, phase = TransactionPhase.AFTER_COMMIT)
    public void handleBookingChangeEvent(BookingChangeEvent event){

        BookingResponse booking = bookingRepository.findByNo(event.getBookingNo()).orElseThrow(BookingNotFoundException::new);

        MeetingRoomResponse room = getMeetingRoom(booking.getRoom().getNo());
        booking.getRoom().setName(room.getMeetingRoomName());

        EmailRequest request = new EmailRequest(
                event.getEmail(),
                "[예약 변경] 회의실 변경되었습니다",
                """
                        <h2>회의실 예약이 변경되었습니다</h2>
                           <p><strong>%s</strong>님, 요청하신 회의실 예약이 아래와 같이 정상적으로 변경되었습니다.</p>
                            <br>
                       
                           <hr>
                            <p>
                                <strong>예약 번호:</strong> %s<br>
                                <strong>회의실:</strong> %s<br>
                                <strong>날짜:</strong> %s<br>
                            </p>
                            <hr>
                            <br>
                        
                           <p>
                                회의 시작 <strong>10분 후</strong>까지 입장 확인이 되지 않을 경우, 해당 예약은 자동으로 취소됩니다.<br>
                                예약 변경이나 취소가 필요하신 경우, 아래 버튼을 눌러 주세요.
                           </p>
                        
                           <div class="cta">
                               <a href="https://aiot2.live/booking/history">예약 확인/변경하기</a>
                           </div>
                            <br>
                        
                           <p style="color: gray; font-size: small;">
                                본 메일은 시스템에 의해 자동 발송되었습니다.<br>
                                문의사항이 있으시면 <a href="mailto:help@aiot2.live">help@aiot2.live</a> 으로 연락 주세요.
                           </p>
                        """.formatted(
                        event.getEmail(),
                        booking.getCode(),
                        booking.getRoom().getName(),
                        booking.getStartsAt().toString().replace("T", " ")),
                ROLE_TYPE
        );

        notifyAdaptor.sendHtmlEmail(request);
    }

    @Async
    @TransactionalEventListener(classes = BookingCancelEvent.class, phase = TransactionPhase.AFTER_COMMIT)
    public void handleBookingCancelEvent(BookingCancelEvent event){

        BookingResponse booking = bookingRepository.findByNo(event.getBookingNo()).orElseThrow(BookingNotFoundException::new);

        MeetingRoomResponse room = getMeetingRoom(booking.getRoom().getNo());
        booking.getRoom().setName(room.getMeetingRoomName());

        EmailRequest request = new EmailRequest(
                event.getEmail(),
                "[알림] 회의실 예약이 취소되었습니다.",
                """
                            <h2>회의실 예약 취소 안내</h2>
                            <p><strong>%s</strong>님, 예약하신 회의실 이용이 아래와 같이 <strong>취소</strong>되었습니다.</p>
                            <br>
                        
                            <hr>
                            <p>
                                <strong>예약 번호:</strong> %s<br>
                                <strong>회의실:</strong> %s<br>
                                <strong>날짜:</strong> %s<br>
                            </p>
                            <hr>
                            <br>
                            <p>필요하시면 <a href="https://aiot2.live/booking">예약 페이지</a>에서 다시 예약해 주세요.</p>
                        
                            <p style="color: gray; font-size: small;">
                                본 메일은 시스템에 의해 자동 발송되었습니다.<br>
                                문의사항이 있으시면 <a href="mailto:help@aiot2.live">help@aiot2.live</a> 으로 연락 주세요.
                            </p>
                        """.formatted(
                        event.getEmail(),
                        booking.getCode(),
                        booking.getRoom().getName(),
                        booking.getStartsAt().toString().replace("T", " ")),
                ROLE_TYPE
        );

        notifyAdaptor.sendHtmlEmail(request);
    }

    private MeetingRoomResponse getMeetingRoom(Long roomNo){
        return meetingRoomAdaptor.getMeetingRoom(roomNo);
    }

}

