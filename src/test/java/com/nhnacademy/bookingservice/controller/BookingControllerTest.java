package com.nhnacademy.bookingservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.bookingservice.common.adaptor.MemberAdaptor;
import com.nhnacademy.bookingservice.common.exception.ForbiddenException;
import com.nhnacademy.bookingservice.common.exception.booking.*;
import com.nhnacademy.bookingservice.common.exception.NotFoundException;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
        when(memberAdaptor.getMemberByEmail("test@test.com")).thenReturn(member);
    }

    @Test
    @DisplayName("예약 성공")
    void registerBooking() throws Exception {
        BookingRegisterRequest request = new BookingRegisterRequest(1L, "2025-04-29", "09:30", "10:30",8);
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
        BookingRegisterRequest request = new BookingRegisterRequest(1L, "2025-04-29", "09:30", "11:30", 8);
        String body = mapper.writeValueAsString(request);
        doThrow(new AlreadyMeetingRoomTimeException()).when(bookingService).register(request, member);
        mockMvc.perform(
                        post("/api/v1/bookings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("X-USER", "test@test.com")
                                .content(body)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.uri").value("/api/v1/bookings"))
                .andDo(print());

    }

    @Test
    @DisplayName("예약 실패 - 회의실 인원 초과")
    void registerBooking_exception_case2() throws Exception {
        BookingRegisterRequest request = new BookingRegisterRequest(1L, "2025-04-29", "09:30", "10:30",8);
        String body = mapper.writeValueAsString(request);
        doThrow(new MeetingRoomCapacityExceededException(8)).when(bookingService).register(request, member);
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
    @DisplayName("예약 실패 - 찾을 수 없는 회원일 경우(회원 정보가 없는 경우)")
    void registerBooking_exception_case4() throws Exception {
        BookingRegisterRequest request = new BookingRegisterRequest(1L, "2025-04-29", "09:30", "10:30", 8);
        String body = mapper.writeValueAsString(request);
        BookingRegisterResponse response = new BookingRegisterResponse(1L);
        doThrow((new NotFoundException())).when(memberAdaptor).getMemberByEmail("test@test.com");
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
        BookingResponse.MemberInfo member1 = new BookingResponse.MemberInfo();
        ReflectionTestUtils.setField(member1, "no", 1L);

        BookingResponse.MeetingRoomInfo room = new BookingResponse.MeetingRoomInfo();
        ReflectionTestUtils.setField(room, "no", 1L);
        room.setName("회의실 A");

        BookingResponse bookingResponse = new BookingResponse(1L, "test", LocalDateTime.parse("2025-04-29T09:30:00"), 8, LocalDateTime.parse("2025-04-29T08:30:00"), LocalDateTime.parse("2025-04-29T09:30:00"),  null, member1, room);

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
                .andExpect(jsonPath("$.member.no").value(1L))
                .andExpect(jsonPath("$.room.name").value("회의실 A"))
                .andDo(print());

    }

    @Test
    @DisplayName("예약 조회 - forbidden")
    void getBooking_exception_case1() throws Exception {
        doThrow(new ForbiddenException()).when(bookingService).getBooking(Mockito.anyLong(), Mockito.any());
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
        doThrow(new BookingNotFoundException()).when(bookingService).getBooking(2L, memberInfo);
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
        BookingResponse.MemberInfo member1 = new BookingResponse.MemberInfo();
        member1.setNo(1L);

        BookingResponse.MeetingRoomInfo room = new BookingResponse.MeetingRoomInfo();
        room.setNo(1L);
        room.setName("회의실 A");

        BookingResponse.MeetingRoomInfo room2 = new BookingResponse.MeetingRoomInfo();
        room2.setNo(2L);
        room2.setName("회의실 B");

        BookingResponse response1 = new BookingResponse(1L, "test", LocalDateTime.parse("2025-04-29T09:30:00"), 9,LocalDateTime.parse("2025-04-29T10:30:00"), LocalDateTime.parse("2025-04-29T08:30:00"), null, member1, room);
        BookingResponse response2 = new BookingResponse(2L, "test1", LocalDateTime.parse("2025-04-28T09:30:00"), 9,LocalDateTime.parse("2025-04-28T10:30:00"), LocalDateTime.parse("2025-04-29T08:30:00"), null, member1, room2);

        when(bookingService.getMemberBookings(Mockito.any())).thenReturn(List.of(response1, response2));

        mockMvc.perform(
                        get("/api/v1/bookings/me/statistics")
                                .header("X-USER", "test@test.com")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].no").value(1L))
                .andExpect(jsonPath("$[0].member.no").value(1L))
                .andExpect(jsonPath("$[0].room.name").value("회의실 A"))
                .andExpect(jsonPath("$[1].no").value(2L))
                .andExpect(jsonPath("$[1].member.no").value(1L))
                .andExpect(jsonPath("$[1].room.name").value("회의실 B"))
                .andDo(print());
    }

    @Test
    @DisplayName("예약 조회(리스트) - 전체")
    void getAllBookings_list() throws Exception {
        BookingResponse.MemberInfo member1 = new BookingResponse.MemberInfo();
        member1.setNo(1L);
        member1.setEmail("test@test.com");
        member1.setName("test");

        BookingResponse.MemberInfo member2 = new BookingResponse.MemberInfo();
        member2.setNo(2L);
        member2.setEmail("test2@test.com");
        member2.setName("test2");

        BookingResponse.MeetingRoomInfo room = new BookingResponse.MeetingRoomInfo();
        room.setNo(1L);
        room.setName("회의실 A");

        BookingResponse.MeetingRoomInfo room2 = new BookingResponse.MeetingRoomInfo();
        room2.setNo(2L);
        room2.setName("회의실 B");

        BookingResponse response1 = new BookingResponse(1L, "test", LocalDateTime.parse("2025-04-29T09:30:00"), 9,LocalDateTime.parse("2025-04-29T10:30:00"), LocalDateTime.parse("2025-04-29T08:30:00"), null, member1, room);
        BookingResponse response2 = new BookingResponse(2L, "test1", LocalDateTime.parse("2025-04-28T09:30:00"), 9,LocalDateTime.parse("2025-04-28T10:30:00"), LocalDateTime.parse("2025-04-29T08:30:00"), null, member2, room2);
        BookingResponse response3 = new BookingResponse(3L, "test2", LocalDateTime.parse("2025-04-30T09:30:00"), 9,LocalDateTime.parse("2025-04-30T10:30:00"), LocalDateTime.parse("2025-04-29T08:30:00"), null, member1, room2);
        BookingResponse response4 = new BookingResponse(4L, "test3", LocalDateTime.parse("2025-04-29T10:30:00"), 9,LocalDateTime.parse("2025-04-29T11:30:00"), LocalDateTime.parse("2025-04-29T08:30:00"), null, member2,room);

        when(bookingService.getBookings()).thenReturn(List.of(response1, response2, response3, response4));

        mockMvc.perform(
                        get("/api/v1/bookings/statistics")
                                .header("X-USER", "test@test.com")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].no").value(1L))
                .andExpect(jsonPath("$[0].member.name").value("test"))
                .andExpect(jsonPath("$[0].room.name").value("회의실 A"))
                .andExpect(jsonPath("$[1].no").value(2L))
                .andExpect(jsonPath("$[1].member.name").value("test2"))
                .andExpect(jsonPath("$[1].room.name").value("회의실 B"))
                .andExpect(jsonPath("$[2].no").value(3L))
                .andExpect(jsonPath("$[2].member.name").value("test"))
                .andExpect(jsonPath("$[2].room.name").value("회의실 B"))
                .andDo(print());
    }

    @Test
    @DisplayName("예약 조회(페이징) - 사용자별")
    void getBookingsByMember() throws Exception {
        BookingResponse.MemberInfo member1 = new BookingResponse.MemberInfo();
        member1.setNo(1L);
        member1.setName("test");

        BookingResponse.MeetingRoomInfo room = new BookingResponse.MeetingRoomInfo();
        room.setNo(1L);
        room.setName("회의실 A");

        BookingResponse.MeetingRoomInfo room2 = new BookingResponse.MeetingRoomInfo();
        room2.setNo(2L);
        room2.setName("회의실 B");

        BookingResponse response1 = new BookingResponse(1L, "test", LocalDateTime.parse("2025-04-29T09:30:00"), 9,LocalDateTime.parse("2025-04-29T10:30:00"), LocalDateTime.parse("2025-04-29T08:30:00"), null, member1, room);
        BookingResponse response2 = new BookingResponse(2L, "test1", LocalDateTime.parse("2025-04-28T09:30:00"), 9,LocalDateTime.parse("2025-04-28T10:30:00"), LocalDateTime.parse("2025-04-29T08:30:00"), null, member1, room2);

        when(bookingService.getPagedMemberBookings(Mockito.any(),Mockito.any())).thenReturn(new PageImpl<>(List.of(response1, response2)));

        mockMvc.perform(
                        get("/api/v1/bookings/me")
                                .header("X-USER", "test@test.com")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.[0].no").value(1L))
                .andExpect(jsonPath("$.content.[0].member.no").value(1L))
                .andExpect(jsonPath("$.content.[0].room.name").value("회의실 A"))
                .andExpect(jsonPath("$.content.[1].no").value(2L))
                .andExpect(jsonPath("$.content.[1].member.no").value(1L))
                .andExpect(jsonPath("$.content.[1].room.name").value("회의실 B"))
                .andDo(print());
    }

    @Test
    @DisplayName("예약 전체 조회(페이징) - 관리자")
    void getAllBookings_admin() throws Exception {
        MemberResponse admin = new MemberResponse(1L, "admin", "admin@test.com", "010-1111-1111" ,"ROLE_ADMIN");
        when(memberAdaptor.getMemberByEmail("admin@test.com")).thenReturn(admin);


        BookingResponse.MemberInfo member1 = new BookingResponse.MemberInfo();
        member1.setNo(1L);
        member1.setEmail("test@test.com");
        member1.setName("test");

        BookingResponse.MemberInfo member2 = new BookingResponse.MemberInfo();
        member2.setNo(2L);
        member2.setEmail("test2@test.com");
        member2.setName("test2");

        BookingResponse.MeetingRoomInfo room = new BookingResponse.MeetingRoomInfo();
        room.setNo(1L);
        room.setName("회의실 A");

        BookingResponse.MeetingRoomInfo room2 = new BookingResponse.MeetingRoomInfo();
        room2.setNo(2L);
        room2.setName("회의실 B");

        BookingResponse response1 = new BookingResponse(1L, "test", LocalDateTime.parse("2025-04-29T09:30:00"), 9,LocalDateTime.parse("2025-04-29T10:30:00"), LocalDateTime.parse("2025-04-29T08:30:00"), null, member1, room);
        BookingResponse response2 = new BookingResponse(2L, "test1", LocalDateTime.parse("2025-04-28T09:30:00"), 9,LocalDateTime.parse("2025-04-28T10:30:00"), LocalDateTime.parse("2025-04-29T08:30:00"), null, member2, room2);
        BookingResponse response3 = new BookingResponse(3L, "test2", LocalDateTime.parse("2025-04-30T09:30:00"), 9,LocalDateTime.parse("2025-04-30T10:30:00"), LocalDateTime.parse("2025-04-29T08:30:00"), null, member1, room2);
        BookingResponse response4 = new BookingResponse(4L, "test3", LocalDateTime.parse("2025-04-29T10:30:00"), 9,LocalDateTime.parse("2025-04-29T11:30:00"), LocalDateTime.parse("2025-04-29T08:30:00"), null, member2, room);

        when(bookingService.getPagedBookings(Pageable.ofSize(10))).thenReturn(new PageImpl<>(List.of(response1, response2, response3, response4)));

        mockMvc.perform(
                        get("/api/v1/bookings")
                                .header("X-USER", "admin@test.com")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.[0].no").value(1L))
                .andExpect(jsonPath("$.content.[0].member.name").value("test"))
                .andExpect(jsonPath("$.content.[0].room.name").value("회의실 A"))
                .andExpect(jsonPath("$.content.[1].no").value(2L))
                .andExpect(jsonPath("$.content.[1].member.name").value("test2"))
                .andExpect(jsonPath("$.content.[1].room.name").value("회의실 B"))
                .andExpect(jsonPath("$.content.[2].no").value(3L))
                .andExpect(jsonPath("$.content.[2].member.name").value("test"))
                .andExpect(jsonPath("$.content.[2].room.name").value("회의실 B"))
                .andDo(print());
    }

    @Test
    @DisplayName("예약 전체 조회(페이징) - 일반 사용자")
    void getAllBookings_user() throws Exception {
        MemberResponse admin = new MemberResponse(1L, "test", "test@test.com", "010-1111-1111" ,"ROLE_USER");
        when(memberAdaptor.getMemberByEmail("admin@test.com")).thenReturn(admin);

        BookingResponse.MemberInfo member1 = new BookingResponse.MemberInfo();
        member1.setNo(1L);
        member1.setEmail("test@test.com");
        member1.setName("test");

        BookingResponse.MemberInfo member2 = new BookingResponse.MemberInfo();
        member2.setNo(2L);
        member2.setEmail("test2@test.com");
        member2.setName("test2");

        BookingResponse.MeetingRoomInfo room = new BookingResponse.MeetingRoomInfo();
        room.setNo(1L);
        room.setName("회의실 A");

        BookingResponse.MeetingRoomInfo room2 = new BookingResponse.MeetingRoomInfo();
        room2.setNo(2L);
        room2.setName("회의실 B");

        BookingResponse response1 = new BookingResponse(1L, "test", LocalDateTime.parse("2025-04-29T09:30:00"), 9,LocalDateTime.parse("2025-04-29T10:30:00"), LocalDateTime.parse("2025-04-29T08:30:00"), null, member1, room);
        BookingResponse response2 = new BookingResponse(2L, "test1", LocalDateTime.parse("2025-04-28T09:30:00"), 9,LocalDateTime.parse("2025-04-28T10:30:00"), LocalDateTime.parse("2025-04-29T08:30:00"), null, member2, room2);
        BookingResponse response3 = new BookingResponse(3L, "test2", LocalDateTime.parse("2025-04-30T09:30:00"), 9,LocalDateTime.parse("2025-04-30T10:30:00"), LocalDateTime.parse("2025-04-29T08:30:00"), null, member1, room2);
        BookingResponse response4 = new BookingResponse(4L, "test3", LocalDateTime.parse("2025-04-29T10:30:00"), 9,LocalDateTime.parse("2025-04-29T11:30:00"), LocalDateTime.parse("2025-04-29T08:30:00"), null, member2, room);

        when(bookingService.getPagedBookings(Pageable.ofSize(10))).thenReturn(new PageImpl<>(List.of(response1, response2, response3, response4)));

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
        when(memberAdaptor.getMemberByEmail("admin@test.com")).thenReturn(admin);

        BookingResponse.MemberInfo member1 = new BookingResponse.MemberInfo();
        member1.setNo(1L);
        member1.setEmail("test@test.com");
        member1.setName("test");

        BookingResponse.MemberInfo member2 = new BookingResponse.MemberInfo();
        member2.setNo(2L);
        member2.setEmail("test2@test.com");
        member2.setName("test2");

        BookingResponse.MeetingRoomInfo room = new BookingResponse.MeetingRoomInfo();
        room.setNo(1L);
        room.setName("회의실 A");

        BookingResponse.MeetingRoomInfo room2 = new BookingResponse.MeetingRoomInfo();
        room2.setNo(2L);
        room2.setName("회의실 B");

        BookingResponse response1 = new BookingResponse(1L, "test", LocalDateTime.parse("2025-04-29T09:30:00"), 9,LocalDateTime.parse("2025-04-29T10:30:00"), LocalDateTime.parse("2025-04-29T08:30:00"), null, member1, room);
        BookingResponse response2 = new BookingResponse(2L, "test1", LocalDateTime.parse("2025-04-28T09:30:00"), 9,LocalDateTime.parse("2025-04-28T10:30:00"), LocalDateTime.parse("2025-04-29T08:30:00"), null, member2, room2);
        BookingResponse response3 = new BookingResponse(3L, "test2", LocalDateTime.parse("2025-04-30T09:30:00"), 9,LocalDateTime.parse("2025-04-30T10:30:00"), LocalDateTime.parse("2025-04-29T08:30:00"), null, member1, room2);
        BookingResponse response4 = new BookingResponse(4L, "test3", LocalDateTime.parse("2025-04-29T10:30:00"), 9,LocalDateTime.parse("2025-04-29T11:30:00"), LocalDateTime.parse("2025-04-29T08:30:00"), null, member2, room);

        when(bookingService.getPagedBookings(
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
        DailyBookingResponse response1 = new DailyBookingResponse(1L, 1L, 8, LocalDateTime.parse("2025-04-29T09:30:00"), LocalDateTime.parse("2025-04-29T10:30:00"), null);
        DailyBookingResponse response2 = new DailyBookingResponse(2L, 1L, 8, LocalDateTime.parse("2025-04-29T10:30:00"), LocalDateTime.parse("2025-04-29T11:30:00"), null);

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
        BookingResponse.MemberInfo member1 = new BookingResponse.MemberInfo();
        member1.setNo(1L);
        member1.setName("test");

        BookingResponse.MeetingRoomInfo room = new BookingResponse.MeetingRoomInfo();
        room.setNo(1L);
        room.setName("회의실 A");

        BookingUpdateRequest request = new BookingUpdateRequest(1L, "2025-04-29", "09:30", "10:30", 9);
        BookingResponse bookingResponse = new BookingResponse(1L, "test", LocalDateTime.parse("2025-04-29T09:30:00"), 9,LocalDateTime.parse("2025-04-29T10:30:00"), LocalDateTime.parse("2025-04-29T08:30:00"),"변경", member1, room);

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
                .andExpect(jsonPath("$.member.no").value(1L))
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

        when(bookingService.checkBooking(Mockito.any(MemberResponse.class), Mockito.anyString(), Mockito.any(LocalDateTime.class), Mockito.anyLong())).thenReturn(isPermitted);

        LocalDateTime entryTime = LocalDateTime.now();

        EntryResponse entryResponse = new EntryResponse(
                HttpStatus.OK.value(),
                "입실이 완료되었습니다.",
                entryTime,
                1L
        );

        EntryRequest entryRequest = new EntryRequest(
                "testCode",
                entryTime,
                1L
        );

        String json = mapper.writeValueAsString(entryRequest);

        mockMvc.perform(post("/api/v1/bookings/verify")
                        .header("X-USER", "test@test.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(entryResponse.getStatusCode()))
                .andExpect(jsonPath("$.entryTime").value(entryResponse.getEntryTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))))
                .andExpect(jsonPath("$.bookingNo").value(entryResponse.getBookingNo()))
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

        doThrow(new BookingInfoDoesNotMatchException()).when(bookingService).checkBooking(Mockito.any(MemberResponse.class), Mockito.anyString(), Mockito.any(LocalDateTime.class), Mockito.anyLong());

        mockMvc.perform(post("/api/v1/bookings/verify")
                        .header("X-USER", "test@test.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("예약정보가 일치하지 않습니다."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.uri").value("/api/v1/bookings/verify"))
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

        doThrow(new BookingTimeNotReachedException()).when(bookingService).checkBooking(Mockito.any(MemberResponse.class), Mockito.anyString(), Mockito.any(LocalDateTime.class), Mockito.anyLong());

        mockMvc.perform(post("/api/v1/bookings/verify")
                    .header("X-USER", "test@test.com")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("예약 시간 10분 전부터 입장 가능합니다."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.uri").value("/api/v1/bookings/verify"))
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

        doThrow(new BookingTimeHasPassedException()).when(bookingService).checkBooking(Mockito.any(MemberResponse.class), Mockito.anyString(), Mockito.any(LocalDateTime.class), Mockito.anyLong());

        mockMvc.perform(post("/api/v1/bookings/verify")
                        .header("X-USER", "test@test.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("예약시간 10분 후까지만 입실 가능합니다."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.uri").value("/api/v1/bookings/verify"))
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