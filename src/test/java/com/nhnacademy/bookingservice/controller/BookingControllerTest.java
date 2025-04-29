package com.nhnacademy.bookingservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.bookingservice.common.adaptor.MemberAdaptor;
import com.nhnacademy.bookingservice.dto.BookingRegisterRequest;
import com.nhnacademy.bookingservice.dto.BookingResponse;
import com.nhnacademy.bookingservice.dto.BookingUpdateRequest;
import com.nhnacademy.bookingservice.dto.MemberResponse;
import com.nhnacademy.bookingservice.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookingService bookingService;

    @MockitoBean
    private MemberAdaptor memberAdaptor;


    @Autowired
    private ObjectMapper mapper;

    MemberResponse member;
    @BeforeEach
    void modelAttribute() {
        member = new MemberResponse(1L, "test");
        when(memberAdaptor.getMember("test@test.com")).thenReturn(ResponseEntity.ok(member));
    }

    @Test
    @DisplayName("예약 성공")
    void registerBooking() throws Exception {
        BookingRegisterRequest request = new BookingRegisterRequest(1L, "2025-04-29", "09:30", 8);
        String body = mapper.writeValueAsString(request);
        doNothing().when(bookingService).register(request);
        mockMvc.perform(
                post("/api/v1/bookings")
                        .header("X-USER", "test@test.com")
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isCreated())
                .andDo(print());

    }

    @Test
    @DisplayName("예약 실패 - 헤더가 없는 경우")
    void registerBooking_exception_case() throws Exception {
        BookingRegisterRequest request = new BookingRegisterRequest(1L, "2025-04-29", "09:30", 8);
        String body = mapper.writeValueAsString(request);
        doNothing().when(bookingService).register(request);
        mockMvc.perform(
                        post("/api/v1/bookings")
                                .content(body)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is4xxClientError())
                .andDo(print());

    }

    @Test
    @DisplayName("예약 조회")
    void getBooking() throws Exception {
        BookingResponse bookingResponse = new BookingResponse(1L, "test", LocalDateTime.parse("2025-04-29T09:30:00"), 8,LocalDateTime.parse("2025-04-29T10:30:00"), LocalDateTime.parse("2025-04-29T08:30:00"), 1L, "test", null, 1L, "회의실 A");
        when(bookingService.getBooking(Mockito.anyLong(), Mockito.any())).thenReturn(bookingResponse);
        mockMvc.perform(
                        get("/api/v1/bookings/{no}", 1L)
                                .header("X-USER", "test@test.com")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.no").value(1L))
                .andExpect(jsonPath("$.code").value("test"))
                .andExpect(jsonPath("$.attendeeCount").value(8))
                .andExpect(jsonPath("$.mbNo").value(1L))
                .andExpect(jsonPath("$.roomName").value("회의실 A"))
                .andDo(print());

    }

    @Test
    @DisplayName("예약 수정")
    void updateBooking() throws Exception {
        BookingUpdateRequest request = new BookingUpdateRequest("2025-04-29", "09:30", 9);
        BookingResponse bookingResponse = new BookingResponse(1L, "test", LocalDateTime.parse("2025-04-29T09:30:00"), 9,LocalDateTime.parse("2025-04-29T10:30:00"), LocalDateTime.parse("2025-04-29T08:30:00"), 1L, "test", "변경", 1L, "회의실 A");

        when(bookingService.updateBooking(Mockito.anyLong(), Mockito.any(), Mockito.any())).thenReturn(bookingResponse);

        mockMvc.perform(
                put("/api/v1/bookings/{no}", 1L)
                        .header("X-USER", "test@test.com")
                        .content(mapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("test"))
                .andExpect(jsonPath("$.mbNo").value(1L))
                .andExpect(jsonPath("$.changeName").value("변경"))
                .andDo(print());
    }

    @Test
    @DisplayName("특이사항 변경")
    void updateBookingChange() throws Exception {
       doNothing().when(bookingService).updateBookingChange(Mockito.anyLong(), Mockito.anyLong());
        mockMvc.perform(
                        put("/api/v1/bookings/{no}/changes/{change-no}", 1L, 2L)
                                .header("X-USER", "test@test.com")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("예약 취소")
    void deleteBooking() throws Exception {
        doNothing().when(bookingService).cancelBooking(Mockito.anyLong(), Mockito.any());

        mockMvc.perform(
                delete("/api/v1/bookings/{no}", 1L)
                        .header("X-USER", "test@test.com")
        )
                .andExpect(status().isNoContent())
                .andDo(print());
    }
}