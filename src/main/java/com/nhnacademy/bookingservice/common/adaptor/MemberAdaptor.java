package com.nhnacademy.bookingservice.common.adaptor;


import com.nhnacademy.bookingservice.dto.MemberResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "member-service", url = "http://localhost:10255", path = "/api/v1/members")
public interface MemberAdaptor {

    @GetMapping("/email/{mbEmail}")
    ResponseEntity<MemberResponse> getMemberByEmail(@PathVariable("mbEmail") String email);

    @GetMapping("/{member-id}")
    ResponseEntity<MemberResponse> getMemberByMbNo(@PathVariable("member-id") Long no);

    @GetMapping("/{no}/name")
    ResponseEntity<MemberResponse> getMemberName(@PathVariable("no") Long no);
}
