package com.nhnacademy.bookingservice.repository.impl;

import com.nhnacademy.bookingservice.entity.Booking;
import com.nhnacademy.bookingservice.entity.QBooking;
import com.nhnacademy.bookingservice.dto.BookingResponse;
import com.nhnacademy.bookingservice.repository.CustomBookingRepository;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

public class CustomBookingRepositoryImpl extends QuerydslRepositorySupport implements CustomBookingRepository {

    public CustomBookingRepositoryImpl() {
        super(Booking.class);
    }

    QBooking qBooking = QBooking.booking;

    @Override
    public BookingResponse findByNo(Long no){
        JPAQueryFactory query = new JPAQueryFactory(getEntityManager());

        return query
                .select(Projections.fields(
                            BookingResponse.class,
                            qBooking.no,
                            qBooking.code,
                            qBooking.date,
                            qBooking.attendees,
                            qBooking.finishedAt,
                            qBooking.createdAt,
                            Expressions.nullExpression(),
                            qBooking.bookingChange.name,
                            Expressions.nullExpression()
                        )
                )
                .from(qBooking)
                .where(qBooking.no.eq(no))
                .orderBy(qBooking.createdAt.desc())
                .fetchOne();
    }
//    public Page<BookingResponse> findAll(Pageable pageable){
//        JPAQueryFactory query = new JPAQueryFactory(getEntityManager());
//
//        query
//                .select(Projections.fields(BookingResponse.class, qBooking.no, qBooking.code, qBooking.date, qBooking.attendees, qBooking.finishedAt, qBooking.createdAt, qBooking.bookingChange.name))
//                .from(qBooking)
//                .orderBy(qBooking.createdAt.desc());
//    }
}
