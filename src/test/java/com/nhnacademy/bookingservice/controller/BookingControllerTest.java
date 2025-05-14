package com.nhnacademy.bookingservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.bookingservice.common.adaptor.MemberAdaptor;
import com.nhnacademy.bookingservice.common.exception.ForbiddenException;
import com.nhnacademy.bookingservice.common.exception.booking.*;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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
        when(memberAdaptor.getMemberByEmail("test@test.com")).thenReturn(ResponseEntity.ok(member));
    }

    @Test
    @DisplayName("예약 성공")
    void registerBooking() throws Exception {
        BookingRegisterRequest request = new BookingRegisterRequest(1L, "2025-04-29", "09:30", 8);
        String body = mapper.writeValueAsString(request);

        BookingRegisterResponse response = new BookingRegisterResponse(1L);
        when(bookingService.register(request)).thenReturn(response);
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
        doThrow(AlreadyMeetingRoomTimeException.class).when(bookingService).register(request);
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
        doThrow(MeetingRoomCapacityExceededException.class).when(bookingService).register(request);
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
        when(bookingService.register(request)).thenReturn(response);
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
        when(memberAdaptor.getMemberByEmail("test@test.com")).thenReturn(ResponseEntity.notFound().build());
        when(bookingService.register(request)).thenReturn(response);
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
        BookingResponse bookingResponse = new BookingResponse(1L, "test", LocalDateTime.parse("2025-04-29T09:30:00"), 8, LocalDateTime.parse("2025-04-29T10:30:00"), LocalDateTime.parse("2025-04-29T11:30:00"), 1L, "test", null, 1L, "회의실 A");
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
        MemberResponse memberInfo = new MemberResponse(1L, "test");
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
    @DisplayName("예약 조회 - 사용자별")
    void getBookingsByMember() throws Exception {
        BookingResponse response1 = new BookingResponse(1L, "test", LocalDateTime.parse("2025-04-29T09:30:00"), 9, LocalDateTime.parse("2025-04-29T10:30:00"), LocalDateTime.parse("2025-04-29T11:30:00"), 1L, "test", null, 1L, "회의실 A");
        BookingResponse response2 = new BookingResponse(2L, "test1", LocalDateTime.parse("2025-04-28T09:30:00"), 9, LocalDateTime.parse("2025-04-28T10:30:00"), LocalDateTime.parse("2025-04-28T11:30:00"), 1L, "test", null, 2L, "회의실 B");

        when(bookingService.getBookingsByMember(Mockito.any(), Mockito.any())).thenReturn(new PageImpl<>(List.of(response1, response2)));

        mockMvc.perform(
                        get("/api/v1/bookings/my")
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
    @DisplayName("예약 조회 - 전체 리스트")
    void getAllBookings() throws Exception {
        BookingResponse response1 = new BookingResponse(1L, "test", LocalDateTime.parse("2025-04-29T09:30:00"), 9, LocalDateTime.parse("2025-04-29T10:30:00"), LocalDateTime.parse("2025-04-29T11:30:00"), 1L, "test", null, 1L, "회의실 A");
        BookingResponse response2 = new BookingResponse(2L, "test1", LocalDateTime.parse("2025-04-28T09:30:00"), 9, LocalDateTime.parse("2025-04-28T10:30:00"), LocalDateTime.parse("2025-04-28T11:30:00"), 2L, "test2", null, 2L, "회의실 B");
        BookingResponse response3 = new BookingResponse(3L, "test2", LocalDateTime.parse("2025-04-30T09:30:00"), 9, LocalDateTime.parse("2025-04-30T10:30:00"), LocalDateTime.parse("2025-04-30T11:30:00"), 1L, "test", null, 2L, "회의실 B");
        BookingResponse response4 = new BookingResponse(4L, "test3", LocalDateTime.parse("2025-04-29T10:30:00"), 9, LocalDateTime.parse("2025-04-29T11:30:00"), LocalDateTime.parse("2025-04-29T12:30:00"), 2L, "test2", null, 1L, "회의실 A");

        when(bookingService.getAllBookings(Pageable.ofSize(10))).thenReturn(new PageImpl<>(List.of(response1, response2, response3, response4)));

        mockMvc.perform(
                        get("/api/v1/bookings")
                                .header("X-USER", "test@test.com")
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
    @DisplayName("예약 조회 - 회의실 날짜별")
    void getDailyBookings() throws Exception {
        DailyBookingResponse response1 = new DailyBookingResponse(1L, 1L, 8, LocalDateTime.parse("2025-04-29T09:30:00"), LocalDateTime.parse("2025-04-29T10:30:00"));
        DailyBookingResponse response2 = new DailyBookingResponse(2L, 1L, 8, LocalDateTime.parse("2025-04-29T10:30:00"), LocalDateTime.parse("2025-04-29T11:30:00"));

        when(bookingService.getDailyBookings(2L, LocalDate.parse("2025-04-29"))).thenReturn(List.of(response1, response2));

        mockMvc.perform(
                        get("/api/v1/bookings/meeting-rooms/{roomNo}/date/{date}", 2L, "2025-04-29")
                                .header("X-USER", "test@test.com")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].no").value(1L))
                .andExpect(jsonPath("$[0].startsAt").value("2025-04-29T09:30:00"))
                .andExpect(jsonPath("$[0].finishesAt").value("2025-04-29T10:30:00"))
                .andExpect(jsonPath("$[1].no").value(2L))
                .andExpect(jsonPath("$[1].startsAt").value("2025-04-29T10:30:00"))
                .andExpect(jsonPath("$[1].finishesAt").value("2025-04-29T11:30:00"))
                .andDo(print());
    }

    @Test
    @DisplayName("예약 수정")
    void updateBooking() throws Exception {
        BookingUpdateRequest request = new BookingUpdateRequest("2025-04-29", "09:30", 9);
        BookingResponse bookingResponse = new BookingResponse(1L, "test", LocalDateTime.parse("2025-04-29T09:30:00"), 9, LocalDateTime.parse("2025-04-29T10:30:00"), LocalDateTime.parse("2025-04-29T08:30:00"), 1L, "test", "변경", 1L, "회의실 A");

        when(bookingService.updateBooking(Mockito.anyLong(), Mockito.any())).thenReturn(bookingResponse);

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
    @DisplayName("입실 시 예약정보 확인")
    void checkBooking() throws Exception {
        boolean isPermitted = true;

        when(bookingService.checkBooking(Mockito.anyLong(), Mockito.anyString(), Mockito.any(LocalDateTime.class), Mockito.anyLong())).thenReturn(isPermitted);

        LocalDateTime entryTime = LocalDateTime.now();

        EntryResponse entryResponse = new EntryResponse(
                "testCode",
                entryTime,
                1L
        );

        EntryRequest entryRequest = new EntryRequest(
                "testCode",
                entryTime,
                1L
        );

        String json = mapper.writeValueAsString(entryRequest);

        mockMvc.perform(post("/api/v1/bookings/{no}/enter", 1L)
                        .header("X-USER", "test@test.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(entryResponse.getCode()))
                .andExpect(jsonPath("$.entryTime").value(entryResponse.getEntryTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))))
                .andExpect(jsonPath("$.meetingRoomNo").value(entryResponse.getMeetingRoomNo()))
                .andDo(print());
    }

    @Test
    @DisplayName("입실불허 - 400 Bad Request")
    void checkBookingCodeFailed() throws Exception {
        boolean isPermitted = false;

        when(bookingService.checkBooking(1L, "testCode", LocalDateTime.now(), 1L)).thenReturn(isPermitted);

        EntryRequest entryRequest = new EntryRequest(
                "testCode",
                LocalDateTime.now(),
                1L
        );

        String json = mapper.writeValueAsString(entryRequest);

        mockMvc.perform(post("/api/v1/bookings/{no}/enter", 1L)
                        .header("X-USER", "test@test.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("입실 날짜가 예약 날짜와 다른 경우 - 입실 불가")
    void checkBookingDifferentDate() throws Exception {
        EntryRequest entryRequest = new EntryRequest(
                "testCode",
                LocalDateTime.now(),
                1L
        );

        String json = mapper.writeValueAsString(entryRequest);

        doThrow(new BookingInfoDoesNotMatchException()).when(bookingService).checkBooking(Mockito.anyLong(), Mockito.anyString(), Mockito.any(LocalDateTime.class), Mockito.anyLong());

        mockMvc.perform(post("/api/v1/bookings/{no}/enter", 1L)
                        .header("X-USER", "test@test.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("예약정보가 일치하지 않습니다."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.uri").value(String.format("/api/v1/bookings/%d/enter", 1)))
                .andDo(print());
    }

    @Test
    @DisplayName("입실이 예약시간보다 10분 이상 빠른 경우 - 입실 불가")
    void checkBookingEarlyEntry() throws Exception {
        EntryRequest entryRequest = new EntryRequest(
                "testCode",
                LocalDateTime.now(),
                1L
        );

        String json = mapper.writeValueAsString(entryRequest);

        doThrow(new BookingTimeNotReachedException()).when(bookingService).checkBooking(Mockito.anyLong(), Mockito.anyString(), Mockito.any(LocalDateTime.class), Mockito.anyLong());

        mockMvc.perform(post("/api/v1/bookings/{no}/enter", 1L)
                    .header("X-USER", "test@test.com")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("예약 시간 10분 전부터 입장 가능합니다."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.uri").value(String.format("/api/v1/bookings/%d/enter", 1)))
                .andDo(print());
    }

    @Test
    @DisplayName("입실이 예약시간보다 10분 이상 늦은 경우 - 입실 불가")
    void checkBookingLateEntry() throws Exception {
        EntryRequest entryRequest = new EntryRequest(
                "testCode",
                LocalDateTime.now(),
                1L
        );

        String json = mapper.writeValueAsString(entryRequest);

        doThrow(new BookingTimeHasPassedException()).when(bookingService).checkBooking(Mockito.anyLong(), Mockito.anyString(), Mockito.any(LocalDateTime.class), Mockito.anyLong());

        mockMvc.perform(post("/api/v1/bookings/{no}/enter", 1L)
                        .header("X-USER", "test@test.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("예약시간 10분 후까지만 입실 가능합니다."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.uri").value(String.format("/api/v1/bookings/%d/enter", 1)))
                .andDo(print());
    }
}