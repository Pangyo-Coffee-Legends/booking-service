package com.nhnacademy.bookingservice.common.adaptor;

import com.nhnacademy.bookingservice.dto.EmailRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "notify-service", url = "http://localhost:10260", path = "/api/v1/email")
public interface NotifyAdaptor {

    @GetMapping("/html")
    String sendHtmlEmail(EmailRequest request);
}
