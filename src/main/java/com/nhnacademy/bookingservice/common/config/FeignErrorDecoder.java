package com.nhnacademy.bookingservice.common.config;

import com.nhnacademy.bookingservice.common.exception.CommonHttpException;
import feign.Response;
import feign.codec.ErrorDecoder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class FeignErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        String message = "Feign Error";
       if(response.body() != null) {
           try (InputStream inputStream = response.body().asInputStream()) {
               message = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
           } catch (IOException e) {
               message = "%s 응답 읽기 실패".formatted(methodKey);
           }
       }

        return new CommonHttpException(message, response.status());
    }
}
