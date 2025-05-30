package com.nhnacademy.bookingservice.common.adaptor;


import com.nhnacademy.bookingservice.dto.ConfirmPasswordRequest;
import com.nhnacademy.bookingservice.dto.MemberResponse;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "member-service", url = "http://localhost:10255", path = "/api/v1/members")
public interface MemberAdaptor {

    @GetMapping("/email/{email}?view=summary")
    MemberResponse getMemberByEmail(@PathVariable("email") String email);

    @GetMapping("/{no}?view=summary")
    MemberResponse getMemberByMbNo(@PathVariable("no") Long no);

    @PostMapping("/{mbNo}/password")
    Boolean verify(
            @PathVariable("mbNo") Long mbNo,
            @RequestBody @Valid ConfirmPasswordRequest request);
}
