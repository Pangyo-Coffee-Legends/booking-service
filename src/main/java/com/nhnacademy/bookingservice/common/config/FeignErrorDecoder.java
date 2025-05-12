package com.nhnacademy.bookingservice.common.config;

import com.nhnacademy.bookingservice.common.exception.NotFoundException;
import feign.Response;
import feign.codec.ErrorDecoder;
import jakarta.ws.rs.InternalServerErrorException;

public class FeignErrorDecoder implements ErrorDecoder {
    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        if(methodKey.contains("MeetingRoomAdaptor")){
            return switch (response.status()) {
                case 404 -> new NotFoundException(response.reason());
                case 500 -> new InternalServerErrorException("meeting-service 서버 내부에 오류가 발생했습니다.");
                default -> new Exception(response.reason());
            };
        } else if (methodKey.contains("MemberAdaptor")){
            return switch (response.status()) {
                case 404 -> new NotFoundException(response.reason());
                case 500 -> new InternalServerErrorException("member-service 서버 내부에 오류가 발생했습니다.");
                default -> new Exception(response.reason());
            };
        }

        return defaultDecoder.decode(methodKey, response);
    }
}
