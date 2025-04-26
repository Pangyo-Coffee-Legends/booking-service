package com.nhnacademy.bookingservice.common.adaptor;

import com.nhnacademy.bookingservice.dto.MeetingRoomResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "meeting-room-service", url = "http://localhost:10257", path = "/api/v1/meetings")
public interface MeetingRoomAdaptor {

    @GetMapping("/meetings/{no}")
    ResponseEntity<MeetingRoomResponse> getMeetingRoom(Long no);
}
