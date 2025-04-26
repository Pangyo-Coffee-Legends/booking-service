package com.nhnacademy.bookingservice.common.adaptor;


import com.nhnacademy.bookingservice.dto.MemberInfoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "member-service", url = "http://localhost:10255", path = "/api/v1/members")
public interface MemberAdaptor {

    @GetMapping("/info/{mbEmail}")
    ResponseEntity<MemberInfoResponse> getMember(@PathVariable String mbEmail);

}
