package com.nhnacademy.bookingservice.repository.impl;

import com.nhnacademy.bookingservice.dto.BookingResponse;
import com.nhnacademy.bookingservice.dto.DailyBookingResponse;
import com.nhnacademy.bookingservice.dto.QBookingResponse;
import com.nhnacademy.bookingservice.dto.QDailyBookingResponse;
import com.nhnacademy.bookingservice.domain.Booking;
import com.nhnacademy.bookingservice.domain.BookingChangeType;
import com.nhnacademy.bookingservice.domain.QBooking;
import com.nhnacademy.bookingservice.domain.QBookingChange;
import com.nhnacademy.bookingservice.repository.CustomBookingRepository;
import com.nhnacademy.bookingservice.repository.util.QueryDslUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class CustomBookingRepositoryImpl implements CustomBookingRepository {

    private final JPAQueryFactory queryFactory;

    QBooking qBooking = QBooking.booking;
    QBookingChange qBookingChange = QBookingChange.bookingChange;

    PathBuilder<Booking> entityPath = new PathBuilder<>(Booking.class, "booking");

    private JPAQuery<BookingResponse> getBookingQuery(JPAQueryFactory queryFactory) {
        return queryFactory
                .select(new QBookingResponse(
                        qBooking.bookingNo.as("no"),
                        qBooking.bookingCode.as("code"),
                        qBooking.bookingDate.as("startsAt"),
                        qBooking.attendeeCount,
                        qBooking.finishesAt,
                        qBooking.createdAt,
                        qBooking.bookingChange.name.as("changeName"),
                        qBooking.mbNo,
                        qBooking.meetingRoomNo
                ))
                .from(qBooking)
                .leftJoin(qBooking.bookingChange, qBookingChange);
    }

    @Override
    public Optional<BookingResponse> findByNo(Long no){

        return Optional.ofNullable(getBookingQuery(queryFactory)
                .where(qBooking.bookingNo.eq(no))
                .orderBy(qBooking.createdAt.desc())
                .fetchOne());
    }

    @Override
    public List<BookingResponse> findBookingList(Long mbNo){

        return getBookingQuery(queryFactory)
                .where(whereExpression(mbNo))
//                .orderBy(qBooking.createdAt.desc())
                .fetch();
    }

    @Override
    public Page<BookingResponse> findBookings(Long mbNo, Pageable pageable){
        JPAQuery<BookingResponse> query = getBookingQuery(queryFactory)
                .where(whereExpression(mbNo));


        List<OrderSpecifier> orderSpecifiers = QueryDslUtil.getOrderSpecifiers(pageable, entityPath);

        List<BookingResponse> bookingList = query
                .orderBy(orderSpecifiers.toArray(new OrderSpecifier[0]))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> count = queryFactory.select(qBooking.count())
                .from(qBooking)
                .where(whereExpression(mbNo));


        return PageableExecutionUtils.getPage(bookingList, pageable, count::fetchOne);
    }

    public BooleanBuilder whereExpression(Long mbNo){
        BooleanBuilder booleanBuilder = new BooleanBuilder();

        if(mbNo != null) {
            booleanBuilder.and(qBooking.mbNo.eq(mbNo));
        }

        return booleanBuilder;
    }

    @Override
    public List<DailyBookingResponse> findBookingsByDate(Long meetingRoomNo, LocalDate date){

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        return queryFactory
                .select(new QDailyBookingResponse(
                        qBooking.bookingNo.as("no"),
                        qBooking.mbNo,
                        qBooking.attendeeCount,
                        qBooking.bookingDate.as("startsAt"),
                        qBooking.finishesAt,
                        qBookingChange.name
                        )
                )
                .from(qBooking)
                .leftJoin(qBooking.bookingChange, qBookingChange)
                .where(qBooking.meetingRoomNo.eq(meetingRoomNo),
                        qBooking.bookingChange.isNull().or(qBooking.bookingChange.no.ne(BookingChangeType.CANCEL.getId())),
                        qBooking.bookingDate.goe(start).and(qBooking.bookingDate.lt(end))
                )
                .fetch();
    }

    @Override
    public boolean existsRoomNoAndDate(Long meetingRoomNo, LocalDateTime date) {

        Long exist = queryFactory.select(qBooking.count())
                .from(qBooking)
                .where(qBooking.meetingRoomNo.eq(meetingRoomNo),
                        qBooking.bookingDate.lt(date.plusHours(1)),
                        qBooking.finishesAt.gt(date)
                )
                .fetchOne();

        return exist != null && exist > 0;
    }

    @Override
    public boolean hasBookingStartingAt(Long meetingRoomNo, LocalDateTime date) {

        Long exist = queryFactory.select(qBooking.count())
                .from(qBooking)
                .where(qBooking.meetingRoomNo.eq(meetingRoomNo), qBooking.bookingDate.eq(date))
                .fetchOne();

        return exist != null && exist > 0;
    }
}
