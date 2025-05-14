package com.nhnacademy.bookingservice.common.advice;

import com.nhnacademy.bookingservice.common.exception.BadRequestException;
import com.nhnacademy.bookingservice.common.exception.ConflictException;
import com.nhnacademy.bookingservice.common.exception.ForbiddenException;
import com.nhnacademy.bookingservice.common.exception.NotFoundException;
import com.nhnacademy.bookingservice.controller.BookingController;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@Slf4j
@RestControllerAdvice(basePackageClasses = BookingController.class)
public class CommonAdvice {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonErrorResponse> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e, HttpServletRequest request) {
        CommonErrorResponse commonErrorResponse = new CommonErrorResponse(
                e.getMessage(),
                HttpStatus.BAD_REQUEST.hashCode(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(commonErrorResponse);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<CommonErrorResponse> missingRequestHeaderExceptionHandler(MissingRequestHeaderException e, HttpServletRequest request) {
        CommonErrorResponse commonErrorResponse = new CommonErrorResponse(
                e.getMessage(),
                HttpStatus.BAD_REQUEST.hashCode(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(commonErrorResponse);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<CommonErrorResponse> badRequestExceptionHandler(BadRequestException e, HttpServletRequest request){
        CommonErrorResponse errorResponse = new CommonErrorResponse(
                e.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<CommonErrorResponse> forbiddenExceptionHandler(ForbiddenException e, HttpServletRequest request){
        CommonErrorResponse errorResponse = new CommonErrorResponse(
                e.getMessage(),
                HttpStatus.FORBIDDEN.value(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
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

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<CommonErrorResponse> conflictExceptionHandler(ConflictException e, HttpServletRequest request){
        CommonErrorResponse errorResponse = new CommonErrorResponse(
                e.getMessage(),
                HttpStatus.CONFLICT.value(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
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
