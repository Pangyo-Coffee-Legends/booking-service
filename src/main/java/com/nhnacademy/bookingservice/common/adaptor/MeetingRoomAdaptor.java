package com.nhnacademy.bookingservice.common.adaptor;

import com.nhnacademy.bookingservice.dto.MeetingRoomResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "meeting-room-service", url = "http://localhost:10258", path = "/api/v1/meeting-rooms")
public interface MeetingRoomAdaptor {

    @GetMapping("/{no}")
    MeetingRoomResponse getMeetingRoom(@PathVariable("no") Long no);
}
