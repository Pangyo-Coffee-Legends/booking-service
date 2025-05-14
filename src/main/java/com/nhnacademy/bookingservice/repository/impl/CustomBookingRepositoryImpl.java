package com.nhnacademy.bookingservice.repository.impl;

import com.nhnacademy.bookingservice.dto.BookingResponse;
import com.nhnacademy.bookingservice.dto.DailyBookingResponse;
import com.nhnacademy.bookingservice.dto.QBookingResponse;
import com.nhnacademy.bookingservice.entity.Booking;
import com.nhnacademy.bookingservice.entity.BookingChangeType;
import com.nhnacademy.bookingservice.entity.QBooking;
import com.nhnacademy.bookingservice.entity.QBookingChange;
import com.nhnacademy.bookingservice.repository.CustomBookingRepository;
import com.nhnacademy.bookingservice.repository.util.QueryDslUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.data.support.PageableExecutionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class CustomBookingRepositoryImpl extends QuerydslRepositorySupport implements CustomBookingRepository {

    public CustomBookingRepositoryImpl() {
        super(Booking.class);
    }

    QBooking qBooking = QBooking.booking;
    QBookingChange qBookingChange = QBookingChange.bookingChange;

    PathBuilder<Booking> entityPath = new PathBuilder<>(Booking.class, "booking");

    private JPAQuery<BookingResponse> getBookingQuery(JPAQueryFactory queryFactory) {
        return queryFactory
                .select(new QBookingResponse(
                        qBooking.bookingNo,
                        qBooking.bookingCode,
                        qBooking.bookingDate,
                        qBooking.attendeeCount,
                        qBooking.finishedAt,
                        qBooking.createdAt,
                        qBooking.mbNo,
                        qBooking.bookingChange.name.as("changeName"),
                        qBooking.roomNo
                ))
                .from(qBooking)
                .leftJoin(qBooking.bookingChange, qBookingChange);
    }

    @Override
    public Optional<BookingResponse> findByNo(Long no){
        JPAQueryFactory query = new JPAQueryFactory(getEntityManager());

        return Optional.ofNullable(getBookingQuery(query)
                .where(qBooking.bookingNo.eq(no))
                .orderBy(qBooking.createdAt.desc())
                .fetchOne());
    }

    @Override
    public List<BookingResponse> findBookingList(Long mbNo){
        JPAQueryFactory query = new JPAQueryFactory(getEntityManager());

        return getBookingQuery(query)
                .where(whereExpression(mbNo))
//                .orderBy(qBooking.createdAt.desc())
                .fetch();
    }

    @Override
    public Page<BookingResponse> findBookings(Long mbNo, Pageable pageable){
        JPAQueryFactory queryFactory = new JPAQueryFactory(getEntityManager());
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
    public List<DailyBookingResponse> findBookingsByDate(Long roomNo, LocalDate date){
        JPAQueryFactory query = new JPAQueryFactory(getEntityManager());

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        return query
                .select(Projections.fields(
                                DailyBookingResponse.class,
                                qBooking.bookingNo.as("no"),
                                qBooking.bookingDate.as("date"),
                                qBooking.finishedAt
                        )
                )
                .from(qBooking)
                .leftJoin(qBooking.bookingChange, qBookingChange)
                .where(qBooking.roomNo.eq(roomNo),
                        qBooking.bookingChange.isNull().or(qBooking.bookingChange.no.ne(BookingChangeType.CANCEL.getId())),
                        qBooking.bookingDate.goe(start).and(qBooking.bookingDate.lt(end))
                )
                .fetch();
    }

    @Override
    public boolean existsRoomNoAndDate(Long roomNo, LocalDateTime date) {
        JPAQueryFactory query = new JPAQueryFactory(getEntityManager());

        Long exist = query.select(qBooking.count())
                .from(qBooking)
                .where(qBooking.roomNo.eq(roomNo),
                        qBooking.bookingDate.lt(date.plusHours(1)),
                        qBooking.finishedAt.gt(date)
                )
                .fetchOne();

        return exist != null && exist > 0;
    }

    @Override
    public boolean hasBookingStartingAt(Long roomNo, LocalDateTime date) {
        JPAQueryFactory query = new JPAQueryFactory(getEntityManager());

        Long exist = query.select(qBooking.count())
                .from(qBooking)
                .where(qBooking.roomNo.eq(roomNo), qBooking.bookingDate.eq(date))
                .fetchOne();

        return exist != null && exist > 0;
    }
}
