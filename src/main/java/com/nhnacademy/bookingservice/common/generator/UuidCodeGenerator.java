package com.nhnacademy.bookingservice.common.generator;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UuidCodeGenerator implements CodeGenerator{
    @Override
    public String generateCode() {
        return UUID.randomUUID().toString().split("-")[0];
    }
}
