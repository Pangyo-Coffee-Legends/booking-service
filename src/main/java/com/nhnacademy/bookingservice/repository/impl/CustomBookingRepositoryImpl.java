package com.nhnacademy.bookingservice.repository.impl;

import com.nhnacademy.bookingservice.dto.BookingResponse;
import com.nhnacademy.bookingservice.dto.QBookingResponse;
import com.nhnacademy.bookingservice.entity.Booking;
import com.nhnacademy.bookingservice.entity.QBooking;
import com.nhnacademy.bookingservice.entity.QBookingChange;
import com.nhnacademy.bookingservice.repository.CustomBookingRepository;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.data.support.PageableExecutionUtils;

import java.time.LocalDateTime;
import java.util.List;

public class CustomBookingRepositoryImpl extends QuerydslRepositorySupport implements CustomBookingRepository {

    public CustomBookingRepositoryImpl() {
        super(Booking.class);
    }

    QBooking qBooking = QBooking.booking;
    QBookingChange qBookingChange = QBookingChange.bookingChange;

    @Override
    public BookingResponse findByNo(Long no){
        JPAQueryFactory query = new JPAQueryFactory(getEntityManager());

        return query
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
                        )
                )
                .from(qBooking)
                .leftJoin(qBookingChange).on(qBookingChange.no.eq(qBooking.bookingChange.no))
                .where(qBooking.bookingNo.eq(no))
                .orderBy(qBooking.createdAt.desc())
                .fetchOne();
    }


    @Override
    public Page<BookingResponse> findBookings(Pageable pageable){
        JPAQueryFactory query = new JPAQueryFactory(getEntityManager());

        List<BookingResponse> bookingList = query
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
                        )
                )
                .from(qBooking)
                .leftJoin(qBookingChange).on(qBookingChange.no.eq(qBooking.bookingChange.no))
                .orderBy(qBooking.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> count = query.select(qBooking.count())
                                    .from(qBooking);

        return PageableExecutionUtils.getPage(bookingList, pageable, count::fetchOne);
    }

    @Override
    public boolean existsRoomNoAndDate(Long roomNo, LocalDateTime date) {
        JPAQueryFactory query = new JPAQueryFactory(getEntityManager());

        Long exist = query.select(qBooking.count())
                .from(qBooking)
                .where(qBooking.roomNo.eq(roomNo).and(qBooking.bookingDate.eq(date)))
                .fetchOne();

        return exist != null && exist > 0;
    }
}
