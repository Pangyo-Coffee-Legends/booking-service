package com.nhnacademy.bookingservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.bookingservice.common.adaptor.MeetingRoomAdaptor;
import com.nhnacademy.bookingservice.common.adaptor.MemberAdaptor;
import com.nhnacademy.bookingservice.common.advice.CommonAdvice;
import com.nhnacademy.bookingservice.common.generator.CodeGenerator;
import com.nhnacademy.bookingservice.dto.*;
import com.nhnacademy.bookingservice.domain.Booking;
import com.nhnacademy.bookingservice.repository.BookingRepository;
import com.nhnacademy.bookingservice.service.BookingService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@Import(CommonAdvice.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BookingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private ObjectMapper mapper;

    @MockitoBean
    CodeGenerator codeGenerator;

    @MockitoBean
    MeetingRoomAdaptor meetingRoomAdaptor;

    @MockitoBean
    MemberAdaptor memberAdaptor;

    MemberResponse member;
    MemberResponse admin;
    MemberResponse other;
    BookingRegisterResponse savedBooking;

    @BeforeEach
    void setUp() {
        member = new MemberResponse(1L, "test", "test@test.com", "010-1111-1111" ,"ROLE_USER");
        when(memberAdaptor.getMemberByEmail("test@test.com")).thenReturn(member);

        admin = new MemberResponse(2L, "admin", "admin@test.com", "010-1111-1111" ,"ROLE_ADMIN");
        when(memberAdaptor.getMemberByEmail("admin@test.com")).thenReturn(admin);

        other = new MemberResponse(3L, "other", "other@test.com", "010-1111-1111" ,"ROLE_USER");
        when(memberAdaptor.getMemberByEmail("other@test.com")).thenReturn(other);

        MeetingRoomResponse meetingRoomResponse = new MeetingRoomResponse(1L, "회의실 A", 10);
        when(meetingRoomAdaptor.getMeetingRoom(1L)).thenReturn(meetingRoomResponse);

        BookingRegisterRequest request = new BookingRegisterRequest(1L, "2025-04-29", "09:30", 8);
        when(codeGenerator.generateCode()).thenReturn("B-CODE-001");
        savedBooking = bookingService.register(request, member);
    }

    @Test
    @Order(1)
    @DisplayName("예약 생성")
    void registerBooking() throws Exception {
        BookingRegisterRequest request2 = new BookingRegisterRequest(1L, "2025-04-29", "11:30", 8);
        String body = mapper.writeValueAsString(request2);
        when(codeGenerator.generateCode()).thenReturn("B-CODE-002");

        mockMvc.perform(
                        post("/api/v1/bookings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("X-USER", "test@test.com")
                                .content(body)
                )
                .andExpect(status().isCreated())
                .andDo(print());

        List<Booking> all = bookingRepository.findAll();
        assertThat(all).hasSize(2);
        assertThat(all.get(1).getMbNo()).isEqualTo(member.getNo());
    }


    @Test
    @Order(2)
    @DisplayName("예약 생성 실패 - 예약 요청 누락")
    void registerBooking_fail_badRequest() throws Exception {
        BookingRegisterRequest request = new BookingRegisterRequest(1L, "2025-04-29", null, null);
        String body = mapper.writeValueAsString(request);
        mockMvc.perform(
                        post("/api/v1/bookings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("X-USER", "test@test.com")
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @Order(3)
    @DisplayName("예약 생성 실패 - 예약 중복")
    void registerBooking_fail_alreadyExist() throws Exception {
        BookingRegisterRequest request = new BookingRegisterRequest(1L, "2025-04-29", "09:30", 8);
        String body = mapper.writeValueAsString(request);
        mockMvc.perform(
                        post("/api/v1/bookings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("X-USER", "test@test.com")
                                .content(body)
                )
                .andExpect(status().isConflict())
                .andDo(print());
    }

    @Test
    @Order(4)
    @DisplayName("예약 생성 실패 - 인원 초과")
    void registerBooking_fail_capacity() throws Exception {
        BookingRegisterRequest request = new BookingRegisterRequest(1L, "2025-04-29", "09:30", 11);
        String body = mapper.writeValueAsString(request);

        mockMvc.perform(
                        post("/api/v1/bookings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("X-USER", "test@test.com")
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @Order(5)
    @DisplayName("예약 생성 실패 - 사용자 헤더 누락")
    void registerBooking_fail_missingHeader() throws Exception {
        BookingRegisterRequest request = new BookingRegisterRequest(1L, "2025-04-29", "09:30", 8);
        String body = mapper.writeValueAsString(request);

        mockMvc.perform(
                        post("/api/v1/bookings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @Order(6)
    @DisplayName("예약 조회")
    void getBooking() throws Exception {
        mockMvc.perform(
                        get("/api/v1/bookings/{no}", savedBooking.getNo())
                                .header("X-USER", "test@test.com")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attendeeCount").value(8))
                .andExpect(jsonPath("$.room.name").value("회의실 A"))
                .andDo(print());
    }

    @Test
    @Order(7)
    @DisplayName("예약 조회 실패 - 찾을 수 없음")
    void getBooking_fail_notfound() throws Exception {
        mockMvc.perform(
                    get("/api/v1/bookings/{no}", 7L)
                            .header("X-USER", "test@test.com")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("예약을 찾을 수 없습니다."))
                .andDo(print());
    }


    @Test
    @Order(8)
    @DisplayName("예약 수정")
    void updateBooking() throws Exception {
        BookingUpdateRequest request = new BookingUpdateRequest("2025-04-29", "09:30", 8, 1L);
        String body = mapper.writeValueAsString(request);

        mockMvc.perform(
                put("/api/v1/bookings/{no}", savedBooking.getNo())
                        .header("X-USER", "test@test.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.changeName").value("변경"))
                .andDo(print());
    }

    @Test
    @Order(9)
    @DisplayName("예약 수정 실패 - 예약자가 아닌 경우")
    void updateBooking_fail_verify() throws Exception {
        BookingUpdateRequest request = new BookingUpdateRequest("2025-04-29", "09:30", 8, 1L);
        String body = mapper.writeValueAsString(request);

        mockMvc.perform(
                        put("/api/v1/bookings/{no}", savedBooking.getNo())
                                .header("X-USER", "admin@test.com")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isForbidden())
                .andDo(print());
    }

    @Test
    @Order(10)
    @DisplayName("예약 연장")
    void extendBooking() throws Exception {
        mockMvc.perform(
                        put("/api/v1/bookings/{no}/extend", savedBooking.getNo())
                                .header("X-USER", "test@test.com")

                )
                .andExpect(status().isOk())
                .andDo(print());

        BookingResponse response = bookingRepository.findByNo(savedBooking.getNo()).orElseThrow();
        Assertions.assertEquals(LocalDateTime.parse("2025-04-29T11:00:00"), response.getFinishesAt());
        Assertions.assertEquals("연장", response.getChangeName());
    }

    @Test
    @Order(11)
    @DisplayName("예약 연장 실패 - 다음 예약 존재")
    void extendBooking_fail_already() throws Exception {
        BookingRegisterRequest request = new BookingRegisterRequest(1L, "2025-04-29", "10:30", 8);
        when(codeGenerator.generateCode()).thenReturn("B-CODE-002");
        bookingService.register(request, member);

        mockMvc.perform(
                        put("/api/v1/bookings/{no}/extend", savedBooking.getNo())
                                .header("X-USER", "test@test.com")

                )
                .andExpect(status().isConflict())
                .andDo(print());

    }

    @Test
    @Order(12)
    @DisplayName("예약 종료")
    void finishBooking() throws Exception {
        mockMvc.perform(
                        put("/api/v1/bookings/{no}/finish", savedBooking.getNo())
                                .header("X-USER", "test@test.com")

                )
                .andExpect(status().isOk())
                .andDo(print());

        BookingResponse response = bookingRepository.findByNo(savedBooking.getNo()).orElseThrow();
        Assertions.assertEquals("종료", response.getChangeName());
    }

    @Test
    @Order(13)
    @DisplayName("예약 취소")
    void deleteBooking() throws Exception {
        mockMvc.perform(
                        delete("/api/v1/bookings/{no}", savedBooking.getNo())
                                .header("X-USER", "test@test.com")

                )
                .andExpect(status().isNoContent())
                .andDo(print());

        BookingResponse response = bookingRepository.findByNo(savedBooking.getNo()).orElseThrow();
        Assertions.assertNull(response.getFinishesAt());
        Assertions.assertEquals("취소", response.getChangeName());
    }

    @Test
    @Order(14)
    @DisplayName("본인 확인")
    void verifyPassword() throws Exception {
        ConfirmPasswordRequest request = new ConfirmPasswordRequest("Test123!");
        String body = mapper.writeValueAsString(request);
        when(memberAdaptor.verify(1L, request)).thenReturn(true);

        mockMvc.perform(
                        post("/api/v1/bookings/{no}/verify", savedBooking.getNo())
                                .header("X-USER", "test@test.com")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @Order(15)
    @DisplayName("본인 확인 - 관리자")
    void verifyPassword_admin() throws Exception {
        ConfirmPasswordRequest request = new ConfirmPasswordRequest("Admin123!");
        String body = mapper.writeValueAsString(request);
        when(memberAdaptor.verify(2L, request)).thenReturn(true);

        mockMvc.perform(
                        post("/api/v1/bookings/{no}/verify", savedBooking.getNo())
                                .header("X-USER", "admin@test.com")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @Order(16)
    @DisplayName("본인 확인 실패 - 일반 사용자")
    void verifyPassword_fail_forbidden() throws Exception {
        ConfirmPasswordRequest request = new ConfirmPasswordRequest("Test123!");
        String body = mapper.writeValueAsString(request);

        mockMvc.perform(
                        post("/api/v1/bookings/{no}/verify", savedBooking.getNo())
                                .header("X-USER", "other@test.com")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isForbidden())
                .andDo(print());
    }

    @Test
    @Order(17)
    @DisplayName("회의실 입실")
    void checkBooking() throws Exception {
        EntryRequest request = new EntryRequest("B-CODE-001", LocalDateTime.parse("2025-04-29T09:20:00"), savedBooking.getNo());
        String body = mapper.writeValueAsString(request);

        mockMvc.perform(
                        post("/api/v1/bookings/verify")
                                .header("X-USER", "test@test.com")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("B-CODE-001"))
                .andDo(print());
    }

    @Test
    @Order(18)
    @DisplayName("회의실 입실 실패 - 코드 불일치")
    void checkBooking_fail_code() throws Exception {
        EntryRequest request = new EntryRequest("test1", LocalDateTime.parse("2025-04-29T09:30:00"), savedBooking.getNo());
        String body = mapper.writeValueAsString(request);

        mockMvc.perform(
                        post("/api/v1/bookings/verify")
                                .header("X-USER", "test@test.com")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    @Order(19)
    @DisplayName("회의실 입실 실패 - 예약 날짜 불일치")
    void checkBooking_fail_startsAt() throws Exception {
        EntryRequest request = new EntryRequest("B-CODE-001", LocalDateTime.parse("2025-04-28T09:30:00"), savedBooking.getNo());
        String body = mapper.writeValueAsString(request);

        mockMvc.perform(
                        post("/api/v1/bookings/verify")
                                .header("X-USER", "test@test.com")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    @Order(20)
    @DisplayName("회의실 입실 실패 - 예약 시간 30분 전")
    void checkBooking_fail_before_minutes() throws Exception {
        EntryRequest request = new EntryRequest("B-CODE-001", LocalDateTime.parse("2025-04-29T09:00:00"), savedBooking.getNo());
        String body = mapper.writeValueAsString(request);

        mockMvc.perform(
                        post("/api/v1/bookings/verify")
                                .header("X-USER", "test@test.com")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @Order(21)
    @DisplayName("회의실 입실 실패 - 예약 시간 30분 후")
    void checkBooking_fail_after_minutes() throws Exception {
        EntryRequest request = new EntryRequest("B-CODE-001", LocalDateTime.parse("2025-04-29T10:00:00"), savedBooking.getNo());
        String body = mapper.writeValueAsString(request);

        mockMvc.perform(
                        post("/api/v1/bookings/verify")
                                .header("X-USER", "test@test.com")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @Order(22)
    @DisplayName("예약 리스트 조회 - 사용자별")
    void getBookingsByMember_list() throws Exception {
        BookingRegisterRequest request2 = new BookingRegisterRequest(1L, "2025-04-29", "11:30", 8);
        BookingRegisterRequest request3 = new BookingRegisterRequest(1L, "2025-04-29", "10:30", 8);
        when(codeGenerator.generateCode()).thenReturn("B-CODE-002");
        bookingService.register(request2, member);
        when(codeGenerator.generateCode()).thenReturn("B-CODE-003");
        bookingService.register(request3, other);

        mockMvc.perform(
                get("/api/v1/bookings/me/statistics")
                        .header("X-USER", "test@test.com")
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$.[0].member.name").value("test"))
                .andExpect(jsonPath("$.[0].startsAt").value("2025-04-29T09:30:00"))
                .andExpect(jsonPath("$.[1].member.name").value("test"))
                .andExpect(jsonPath("$.[1].startsAt").value("2025-04-29T11:30:00"))
                .andDo(print());

    }

    @Test
    @Order(23)
    @DisplayName("예약 페이지 조회 - 사용자별")
    void getBookingsByMember_page() throws Exception {
        BookingRegisterRequest request2 = new BookingRegisterRequest(1L, "2025-04-29", "11:30", 8);
        BookingRegisterRequest request3 = new BookingRegisterRequest(1L, "2025-04-29", "10:30", 8);
        when(codeGenerator.generateCode()).thenReturn("B-CODE-002");
        bookingService.register(request2, member);
        when(codeGenerator.generateCode()).thenReturn("B-CODE-003");
        bookingService.register(request3, member);

        mockMvc.perform(
                        get("/api/v1/bookings/me")
                                .param("sort", "bookingDate", "asc")
                                .param("size", "3")
                                .param("page", "1")
                                .header("X-USER", "test@test.com")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.[0].startsAt").value("2025-04-29T09:30:00"))
                .andExpect(jsonPath("$.content.[1].startsAt").value("2025-04-29T10:30:00"))
                .andExpect(jsonPath("$.content.[2].startsAt").value("2025-04-29T11:30:00"))
                .andDo(print());
    }


    @Test
    @Order(24)
    @DisplayName("예약 리스트 조회 - 전체")
    void getAllBookings_list() throws Exception {
        BookingRegisterRequest request2 = new BookingRegisterRequest(1L, "2025-04-29", "11:30", 8);
        BookingRegisterRequest request3 = new BookingRegisterRequest(1L, "2025-04-29", "10:30", 8);
        when(codeGenerator.generateCode()).thenReturn("B-CODE-002");
        bookingService.register(request2, admin);
        when(codeGenerator.generateCode()).thenReturn("B-CODE-003");
        bookingService.register(request3, other);

        when(memberAdaptor.getMemberByMbNo(1L)).thenReturn(member);
        when(memberAdaptor.getMemberByMbNo(2L)).thenReturn(admin);
        when(memberAdaptor.getMemberByMbNo(3L)).thenReturn(other);

        mockMvc.perform(
                        get("/api/v1/bookings/statistics")
                                .header("X-USER", "admin@test.com")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(3))
                .andExpect(jsonPath("$.[0].member.name").value("test"))
                .andExpect(jsonPath("$.[0].startsAt").value("2025-04-29T09:30:00"))
                .andExpect(jsonPath("$.[1].member.name").value("admin"))
                .andExpect(jsonPath("$.[1].startsAt").value("2025-04-29T11:30:00"))
                .andExpect(jsonPath("$.[2].member.name").value("other"))
                .andExpect(jsonPath("$.[2].startsAt").value("2025-04-29T10:30:00"))
                .andDo(print());
    }

    @Test
    @Order(25)
    @DisplayName("예약 페이지 조회 - 전체 - 관리자")
    void getAllBookings_page() throws Exception {
        BookingRegisterRequest request2 = new BookingRegisterRequest(1L, "2025-04-29", "11:30", 8);
        BookingRegisterRequest request3 = new BookingRegisterRequest(1L, "2025-04-29", "10:30", 8);
        when(codeGenerator.generateCode()).thenReturn("B-CODE-002");
        bookingService.register(request2, admin);
        when(codeGenerator.generateCode()).thenReturn("B-CODE-003");
        bookingService.register(request3, other);

        when(memberAdaptor.getMemberByMbNo(1L)).thenReturn(member);
        when(memberAdaptor.getMemberByMbNo(2L)).thenReturn(admin);
        when(memberAdaptor.getMemberByMbNo(3L)).thenReturn(other);

        mockMvc.perform(
                        get("/api/v1/bookings")
                                .param("sort", "bookingDate", "asc")
                                .param("page", "1")
                                .header("X-USER", "admin@test.com")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.[0].member.name").value("test"))
                .andExpect(jsonPath("$.content.[0].startsAt").value("2025-04-29T09:30:00"))
                .andExpect(jsonPath("$.content.[1].member.name").value("other"))
                .andExpect(jsonPath("$.content.[1].startsAt").value("2025-04-29T10:30:00"))
                .andExpect(jsonPath("$.content.[2].member.name").value("admin"))
                .andExpect(jsonPath("$.content.[2].startsAt").value("2025-04-29T11:30:00"))
                .andDo(print());
    }

    @Test
    @Order(26)
    @DisplayName("예약 페이지 조회 - 전체 - 사용자")
    void getAllBookings_page_fail_forbidden() throws Exception {
        mockMvc.perform(
                        get("/api/v1/bookings")
                                .header("X-USER", "test@test.com")
                )
                .andExpect(status().isForbidden())
                .andDo(print());
    }

    @Test
    @Order(27)
    @DisplayName("예약 리스트 조회 - 특정 회의실, 특정 날짜 ")
    void getDailyBookings() throws Exception {
        MeetingRoomResponse meetingRoomResponse = new MeetingRoomResponse(2L, "회의실 B", 10);
        when(meetingRoomAdaptor.getMeetingRoom(2L)).thenReturn(meetingRoomResponse);

        BookingRegisterRequest request2 = new BookingRegisterRequest(1L, "2025-04-29", "11:30", 8);
        BookingRegisterRequest request3 = new BookingRegisterRequest(2L, "2025-04-29", "10:30", 8);
        when(codeGenerator.generateCode()).thenReturn("B-CODE-002");
        bookingService.register(request2, admin);
        when(codeGenerator.generateCode()).thenReturn("B-CODE-003");
        bookingService.register(request3, other);

        when(memberAdaptor.getMemberByMbNo(1L)).thenReturn(member);
        when(memberAdaptor.getMemberByMbNo(2L)).thenReturn(admin);

        mockMvc.perform(
                        get("/api/v1/bookings/meeting-rooms/{roomNo}/date/{date}", 1L, "2025-04-29")
                                .header("X-USER", "test@test.com")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$.[0].startsAt").value("2025-04-29T09:30:00"))
                .andExpect(jsonPath("$.[1].startsAt").value("2025-04-29T11:30:00"))
                .andDo(print());
    }

}
