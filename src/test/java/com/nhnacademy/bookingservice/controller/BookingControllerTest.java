package com.nhnacademy.bookingservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.bookingservice.common.adaptor.MemberAdaptor;
import com.nhnacademy.bookingservice.common.exception.ForbiddenException;
import com.nhnacademy.bookingservice.common.exception.NotFoundException;
import com.nhnacademy.bookingservice.common.exception.booking.AlreadyMeetingRoomTimeException;
import com.nhnacademy.bookingservice.common.exception.booking.BookingNotFoundException;
import com.nhnacademy.bookingservice.common.exception.meeting.MeetingRoomCapacityExceededException;
import com.nhnacademy.bookingservice.dto.*;
import com.nhnacademy.bookingservice.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

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
    BookingService bookingService;

    @MockitoBean
    MemberAdaptor memberAdaptor;


    @Autowired
    private ObjectMapper mapper;

    MemberResponse member;
    @BeforeEach
    void modelAttribute() {
        member = new MemberResponse(1L, "test", "test@test.com", "010-1111-1111" ,"ROLE_USER");
        when(memberAdaptor.getMember("test@test.com")).thenReturn(member);
    }

    @Test
    @DisplayName("예약 성공")
    void registerBooking() throws Exception {
        BookingRegisterRequest request = new BookingRegisterRequest(1L, "2025-04-29", "09:30", 8);
        String body = mapper.writeValueAsString(request);

        BookingRegisterResponse response = new BookingRegisterResponse(1L);
        when(bookingService.register(request, member)).thenReturn(response);
        mockMvc.perform(
                post("/api/v1/bookings")
                        .header("X-USER", "test@test.com")
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.no").value(1L))
                .andDo(print());

    }

    @Test
    @DisplayName("예약 실패 - 중복")
    void registerBooking_exception_case1() throws Exception {
        BookingRegisterRequest request = new BookingRegisterRequest(1L, "2025-04-29", "09:30", 8);
        String body = mapper.writeValueAsString(request);
        doThrow(AlreadyMeetingRoomTimeException.class).when(bookingService).register(request, member);
        mockMvc.perform(
                        post("/api/v1/bookings")
                                .header("X-USER", "test@test.com")
                                .content(body)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isConflict())
                .andDo(print());

    }

    @Test
    @DisplayName("예약 실패 - bad request")
    void registerBooking_exception_case2() throws Exception {
        BookingRegisterRequest request = new BookingRegisterRequest(1L, "2025-04-29", "09:30", 8);
        String body = mapper.writeValueAsString(request);
        doThrow(MeetingRoomCapacityExceededException.class).when(bookingService).register(request, member);
        mockMvc.perform(
                        post("/api/v1/bookings")
                                .header("X-USER", "test@test.com")
                                .content(body)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andDo(print());

    }

    @Test
    @DisplayName("예약 실패 - 헤더가 없는 경우")
    void registerBooking_exception_case3() throws Exception {
        BookingRegisterRequest request = new BookingRegisterRequest(1L, "2025-04-29", "09:30", 8);
        String body = mapper.writeValueAsString(request);
        BookingRegisterResponse response = new BookingRegisterResponse(1L);
        when(bookingService.register(request, member)).thenReturn(response);
        mockMvc.perform(
                        post("/api/v1/bookings")
                                .content(body)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is4xxClientError())
                .andDo(print());

    }

    @Test
    @DisplayName("예약 실패 - 헤더가 있는 경우")
    void registerBooking_exception_case4() throws Exception {
        BookingRegisterRequest request = new BookingRegisterRequest(1L, "2025-04-29", "09:30", 8);
        String body = mapper.writeValueAsString(request);
        BookingRegisterResponse response = new BookingRegisterResponse(1L);
        when(memberAdaptor.getMember("test@test.com")).thenThrow(NotFoundException.class);
        when(bookingService.register(request, member)).thenReturn(response);
        mockMvc.perform(
                        post("/api/v1/bookings")
                                .header("X-USER", "test@test.com")
                                .content(body)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is4xxClientError())
                .andDo(print());

    }

    @Test
    @DisplayName("예약 조회 - 번호")
    void getBooking() throws Exception {
        BookingResponse bookingResponse = new BookingResponse(1L, "test", LocalDateTime.parse("2025-04-29T09:30:00"), 8, LocalDateTime.parse("2025-04-29T08:30:00"), LocalDateTime.parse("2025-04-29T09:30:00"), 1L, "test", null,null, 1L, "회의실 A");
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
    @DisplayName("예약 조회 - forbidden")
    void getBooking_exception_case1() throws Exception {
        doThrow(ForbiddenException.class).when(bookingService).getBooking(Mockito.anyLong(), Mockito.any());
        mockMvc.perform(
                        get("/api/v1/bookings/{no}", 1L)
                                .header("X-USER", "test@test.com")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden())
                .andDo(print());

    }

    @Test
    @DisplayName("예약 조회 - not found")
    void getBooking_exception_case2() throws Exception {
        MemberResponse memberInfo = new MemberResponse(1L, "test", "test@test.com", "010-1111-1111", "ROLE_USER");
        doThrow(BookingNotFoundException.class).when(bookingService).getBooking(2L, memberInfo);
        mockMvc.perform(
                        get("/api/v1/bookings/{no}", 2L)
                                .header("X-USER", "test@test.com")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound())
                .andDo(print());

    }

    @Test
    @DisplayName("예약 조회(리스트) - 사용자별")
    void getBookingsByMember_list() throws Exception {
        BookingResponse response1 = new BookingResponse(1L, "test", LocalDateTime.parse("2025-04-29T09:30:00"), 9,LocalDateTime.parse("2025-04-29T10:30:00"), LocalDateTime.parse("2025-04-29T08:30:00"), 1L, "test", null, null, 1L, "회의실 A");
        BookingResponse response2 = new BookingResponse(2L, "test1", LocalDateTime.parse("2025-04-28T09:30:00"), 9,LocalDateTime.parse("2025-04-28T10:30:00"), LocalDateTime.parse("2025-04-29T08:30:00"), 1L, "test", null, null, 2L, "회의실 B");

        when(bookingService.getBookingsByMember(Mockito.any())).thenReturn(List.of(response1, response2));

        mockMvc.perform(
                        get("/api/v1/bookings/me/statistics")
                                .header("X-USER", "test@test.com")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].no").value(1L))
                .andExpect(jsonPath("$[0].mbNo").value(1L))
                .andExpect(jsonPath("$[0].roomName").value("회의실 A"))
                .andExpect(jsonPath("$[1].no").value(2L))
                .andExpect(jsonPath("$[1].mbNo").value(1L))
                .andExpect(jsonPath("$[1].roomName").value("회의실 B"))
                .andDo(print());
    }

    @Test
    @DisplayName("예약 조회(리스트) - 전체")
    void getAllBookings_list() throws Exception {
        BookingResponse response1 = new BookingResponse(1L, "test", LocalDateTime.parse("2025-04-29T09:30:00"), 9,LocalDateTime.parse("2025-04-29T10:30:00"), LocalDateTime.parse("2025-04-29T08:30:00"), 1L, "test", "test@test.com", null, 1L, "회의실 A");
        BookingResponse response2 = new BookingResponse(2L, "test1", LocalDateTime.parse("2025-04-28T09:30:00"), 9,LocalDateTime.parse("2025-04-28T10:30:00"), LocalDateTime.parse("2025-04-29T08:30:00"), 2L, "test2", "test2@test.com", null, 2L, "회의실 B");
        BookingResponse response3 = new BookingResponse(3L, "test2", LocalDateTime.parse("2025-04-30T09:30:00"), 9,LocalDateTime.parse("2025-04-30T10:30:00"), LocalDateTime.parse("2025-04-29T08:30:00"), 1L, "test", "test@test.com", null, 2L, "회의실 B");
        BookingResponse response4 = new BookingResponse(4L, "test3", LocalDateTime.parse("2025-04-29T10:30:00"), 9,LocalDateTime.parse("2025-04-29T11:30:00"), LocalDateTime.parse("2025-04-29T08:30:00"), 2L, "test2", "test2@test.com", null, 1L, "회의실 A");

        when(bookingService.getAllBookings()).thenReturn(List.of(response1, response2, response3, response4));

        mockMvc.perform(
                        get("/api/v1/bookings/statistics")
                                .header("X-USER", "test@test.com")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].no").value(1L))
                .andExpect(jsonPath("$[0].mbName").value("test"))
                .andExpect(jsonPath("$[0].roomName").value("회의실 A"))
                .andExpect(jsonPath("$[1].no").value(2L))
                .andExpect(jsonPath("$[1].mbName").value("test2"))
                .andExpect(jsonPath("$[1].roomName").value("회의실 B"))
                .andExpect(jsonPath("$[2].no").value(3L))
                .andExpect(jsonPath("$[2].mbName").value("test"))
                .andExpect(jsonPath("$[2].roomName").value("회의실 B"))
                .andDo(print());
    }

    @Test
    @DisplayName("예약 조회(페이징) - 사용자별")
    void getBookingsByMember() throws Exception {
        BookingResponse response1 = new BookingResponse(1L, "test", LocalDateTime.parse("2025-04-29T09:30:00"), 9,LocalDateTime.parse("2025-04-29T10:30:00"), LocalDateTime.parse("2025-04-29T08:30:00"), 1L, "test", null, null, 1L, "회의실 A");
        BookingResponse response2 = new BookingResponse(2L, "test1", LocalDateTime.parse("2025-04-28T09:30:00"), 9,LocalDateTime.parse("2025-04-28T10:30:00"), LocalDateTime.parse("2025-04-29T08:30:00"), 1L, "test", null, null, 2L, "회의실 B");

        when(bookingService.getBookingsByMember(Mockito.any(),Mockito.any())).thenReturn(new PageImpl<>(List.of(response1, response2)));

        mockMvc.perform(
                        get("/api/v1/bookings/me")
                                .header("X-USER", "test@test.com")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.[0].no").value(1L))
                .andExpect(jsonPath("$.content.[0].mbNo").value(1L))
                .andExpect(jsonPath("$.content.[0].roomName").value("회의실 A"))
                .andExpect(jsonPath("$.content.[1].no").value(2L))
                .andExpect(jsonPath("$.content.[1].mbNo").value(1L))
                .andExpect(jsonPath("$.content.[1].roomName").value("회의실 B"))
                .andDo(print());
    }

    @Test
    @DisplayName("예약 전체 조회(페이징) - 관리자")
    void getAllBookings_admin() throws Exception {
        MemberResponse admin = new MemberResponse(1L, "admin", "admin@test.com", "010-1111-1111" ,"ROLE_ADMIN");
        when(memberAdaptor.getMember("admin@test.com")).thenReturn(admin);

        BookingResponse response1 = new BookingResponse(1L, "test", LocalDateTime.parse("2025-04-29T09:30:00"), 9,LocalDateTime.parse("2025-04-29T10:30:00"), LocalDateTime.parse("2025-04-29T08:30:00"), 1L, "test", "test@test.com", null, 1L, "회의실 A");
        BookingResponse response2 = new BookingResponse(2L, "test1", LocalDateTime.parse("2025-04-28T09:30:00"), 9,LocalDateTime.parse("2025-04-28T10:30:00"), LocalDateTime.parse("2025-04-29T08:30:00"), 2L, "test2", "test2@test.com", null, 2L, "회의실 B");
        BookingResponse response3 = new BookingResponse(3L, "test2", LocalDateTime.parse("2025-04-30T09:30:00"), 9,LocalDateTime.parse("2025-04-30T10:30:00"), LocalDateTime.parse("2025-04-29T08:30:00"), 1L, "test", "test@test.com", null, 2L, "회의실 B");
        BookingResponse response4 = new BookingResponse(4L, "test3", LocalDateTime.parse("2025-04-29T10:30:00"), 9,LocalDateTime.parse("2025-04-29T11:30:00"), LocalDateTime.parse("2025-04-29T08:30:00"), 2L, "test2", "test2@test.com", null, 1L, "회의실 A");

        when(bookingService.getAllBookings(Pageable.ofSize(10))).thenReturn(new PageImpl<>(List.of(response1, response2, response3, response4)));

        mockMvc.perform(
                        get("/api/v1/bookings")
                                .header("X-USER", "admin@test.com")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.[0].no").value(1L))
                .andExpect(jsonPath("$.content.[0].mbName").value("test"))
                .andExpect(jsonPath("$.content.[0].roomName").value("회의실 A"))
                .andExpect(jsonPath("$.content.[1].no").value(2L))
                .andExpect(jsonPath("$.content.[1].mbName").value("test2"))
                .andExpect(jsonPath("$.content.[1].roomName").value("회의실 B"))
                .andExpect(jsonPath("$.content.[2].no").value(3L))
                .andExpect(jsonPath("$.content.[2].mbName").value("test"))
                .andExpect(jsonPath("$.content.[2].roomName").value("회의실 B"))
                .andDo(print());
    }

    @Test
    @DisplayName("예약 전체 조회(페이징) - 일반 사용자")
    void getAllBookings_user() throws Exception {
        MemberResponse admin = new MemberResponse(1L, "test", "test@test.com", "010-1111-1111" ,"ROLE_USER");
        when(memberAdaptor.getMember("admin@test.com")).thenReturn(admin);

        BookingResponse response1 = new BookingResponse(1L, "test", LocalDateTime.parse("2025-04-29T09:30:00"), 9,LocalDateTime.parse("2025-04-29T10:30:00"), LocalDateTime.parse("2025-04-29T08:30:00"), 1L, "test", "test@test.com", null, 1L, "회의실 A");
        BookingResponse response2 = new BookingResponse(2L, "test1", LocalDateTime.parse("2025-04-28T09:30:00"), 9,LocalDateTime.parse("2025-04-28T10:30:00"), LocalDateTime.parse("2025-04-29T08:30:00"), 2L, "test2", "test2@test.com", null, 2L, "회의실 B");
        BookingResponse response3 = new BookingResponse(3L, "test2", LocalDateTime.parse("2025-04-30T09:30:00"), 9,LocalDateTime.parse("2025-04-30T10:30:00"), LocalDateTime.parse("2025-04-29T08:30:00"), 1L, "test", "test@test.com", null, 2L, "회의실 B");
        BookingResponse response4 = new BookingResponse(4L, "test3", LocalDateTime.parse("2025-04-29T10:30:00"), 9,LocalDateTime.parse("2025-04-29T11:30:00"), LocalDateTime.parse("2025-04-29T08:30:00"), 2L, "test2", "test2@test.com", null, 1L, "회의실 A");

        when(bookingService.getAllBookings(Pageable.ofSize(10))).thenReturn(new PageImpl<>(List.of(response1, response2, response3, response4)));

        mockMvc.perform(
                        get("/api/v1/bookings")
                                .header("X-USER", "test@test.com")
                )
                .andExpect(status().isForbidden())
                .andDo(print());
    }

    @Test
    @DisplayName("예약 조회(페이징) 정렬")
    void getAllBookings_sort() throws Exception {

        MemberResponse admin = new MemberResponse(3L, "admin", "admin@test.com", "010-1111-1111" ,"ROLE_ADMIN");
        when(memberAdaptor.getMember("admin@test.com")).thenReturn(admin);

        BookingResponse response1 = new BookingResponse(1L, "test", LocalDateTime.parse("2025-04-29T09:30:00"), 9,LocalDateTime.parse("2025-04-29T10:30:00"), LocalDateTime.parse("2025-04-29T08:30:00"), 1L, "test", "test@test.com", null, 1L, "회의실 A");
        BookingResponse response2 = new BookingResponse(2L, "test1", LocalDateTime.parse("2025-04-28T09:30:00"), 9,LocalDateTime.parse("2025-04-28T10:30:00"), LocalDateTime.parse("2025-04-29T08:30:00"), 2L, "test2", "test2@test.com", null, 2L, "회의실 B");
        BookingResponse response3 = new BookingResponse(3L, "test2", LocalDateTime.parse("2025-04-30T09:30:00"), 9,LocalDateTime.parse("2025-04-30T10:30:00"), LocalDateTime.parse("2025-04-29T08:30:00"), 1L, "test", "test@test.com", null, 2L, "회의실 B");
        BookingResponse response4 = new BookingResponse(4L, "test3", LocalDateTime.parse("2025-04-29T10:30:00"), 9,LocalDateTime.parse("2025-04-29T11:30:00"), LocalDateTime.parse("2025-04-29T08:30:00"), 2L, "test2", "test2@test.com", null, 1L, "회의실 A");

        when(bookingService.getAllBookings(
                argThat(pageable -> pageable.getSort().getOrderFor("bookingDate") != null
                        && Objects.requireNonNull(pageable.getSort().getOrderFor("bookingDate")).getDirection().isDescending())
        )).thenReturn(new PageImpl<>(List.of(response3, response4, response1, response2)));
        mockMvc.perform(
                        get("/api/v1/bookings?sort={field},{direction}", "bookingDate", "desc")
                                .header("X-USER", "admin@test.com")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.[0].no").value(3L))
                .andExpect(jsonPath("$.content.[1].no").value(4L))
                .andExpect(jsonPath("$.content.[2].no").value(1L))
                .andExpect(jsonPath("$.content.[3].no").value(2L))
                .andDo(print());
    }


    @Test
    @DisplayName("예약 조회 - 회의실 날짜별")
    void getDailyBookings() throws Exception {
        DailyBookingResponse response1 = new DailyBookingResponse(1L, LocalDateTime.parse("2025-04-29T09:30:00"), LocalDateTime.parse("2025-04-29T10:30:00"));
        DailyBookingResponse response2 = new DailyBookingResponse(2L, LocalDateTime.parse("2025-04-29T10:30:00"), LocalDateTime.parse("2025-04-29T11:30:00"));


        when(bookingService.getDailyBookings(2L, LocalDate.parse("2025-04-29"))).thenReturn(List.of(response1, response2));

        mockMvc.perform(
                        get("/api/v1/bookings/meeting-rooms/{roomNo}/date/{date}", 2L, "2025-04-29")
                                .header("X-USER", "test@test.com")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].no").value(1L))
                .andExpect(jsonPath("$[0].date").value("2025-04-29T09:30:00"))
                .andExpect(jsonPath("$[0].finishedAt").value("2025-04-29T10:30:00"))
                .andExpect(jsonPath("$[1].no").value(2L))
                .andExpect(jsonPath("$[1].date").value("2025-04-29T10:30:00"))
                .andExpect(jsonPath("$[1].finishedAt").value("2025-04-29T11:30:00"))
                .andDo(print());
    }

    @Test
    @DisplayName("예약 수정")
    void updateBooking() throws Exception {
        BookingUpdateRequest request = new BookingUpdateRequest("2025-04-29", "09:30", 9, 1L);
        BookingResponse bookingResponse = new BookingResponse(1L, "test", LocalDateTime.parse("2025-04-29T09:30:00"), 9,LocalDateTime.parse("2025-04-29T10:30:00"), LocalDateTime.parse("2025-04-29T08:30:00"), 1L, "test", null, "변경", 1L, "회의실 A");

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
    @DisplayName("예약 연장")
    void extendBooking() throws Exception {
        doNothing().when(bookingService).extendBooking(Mockito.anyLong());
        mockMvc.perform(
                        put("/api/v1/bookings/{no}/extend", 1L)
                                .header("X-USER", "test@test.com")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("예약 종료")
    void finishBooking() throws Exception {
        doNothing().when(bookingService).extendBooking(Mockito.anyLong());
        mockMvc.perform(
                        put("/api/v1/bookings/{no}/finish", 1L)
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

    @Test
    @DisplayName("본인인증")
    void verify() throws Exception {
        ConfirmPasswordRequest request = new ConfirmPasswordRequest("test123!");
        String body = mapper.writeValueAsString(request);

        when(memberAdaptor.verify(1L, request)).thenReturn(true);
        mockMvc.perform(
                        post("/api/v1/bookings/{no}/verify", 1L)
                                .header("X-USER", "test@test.com")
                                .content(body)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(print());

    }
}