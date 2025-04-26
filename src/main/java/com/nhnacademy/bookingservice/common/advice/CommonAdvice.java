package com.nhnacademy.bookingservice.common.advice;

import com.nhnacademy.bookingservice.common.exception.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class CommonAdvice {
    @ExceptionHandler(BindException.class)
    public ResponseEntity<CommonErrorResponse> bindExceptionHandler(BindException e, HttpServletRequest request) {
        log.error(e.getMessage(), e);
//        String referer = httpServletRequest.getHeader("Referer");

        List<String> erros = new ArrayList<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            if(error instanceof FieldError fieldError){
                erros.add("%s : %s".formatted(fieldError.getField(),fieldError.getDefaultMessage()));
            }
        });

        CommonErrorResponse commonErrorResponse = new CommonErrorResponse(Strings.join(erros,','), HttpStatus.BAD_REQUEST.hashCode(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(commonErrorResponse);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<CommonErrorResponse> notFoundExceptionHandler(NotFoundException e, HttpServletRequest request){
        CommonErrorResponse errorResponse = new CommonErrorResponse(
                e.getMessage(),
                HttpStatus.NOT_FOUND.value(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<CommonErrorResponse> exceptionHandler(Throwable e, HttpServletRequest request){
        CommonErrorResponse errorResponse = new CommonErrorResponse(
                e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

}
