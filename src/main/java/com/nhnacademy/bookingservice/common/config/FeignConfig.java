package com.nhnacademy.bookingservice.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public FeignErrorDecoder errorDecoder() {
        return new FeignErrorDecoder();
    }
}
