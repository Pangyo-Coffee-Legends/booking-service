DROP TABLE IF EXISTS booking_changes;
CREATE TABLE booking_changes (
                                 change_no bigint NOT NULL COMMENT '특이사항 번호, autoincrement',
                                 change_name varchar(10) NOT NULL COMMENT '특이사항 이름',

                                 CONSTRAINT pk_change PRIMARY KEY (change_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
INSERT INTO bookint_chages(chnage_no, change_name) VALUES (1, '연장'),
                                                          (2, '종료'),
                                                          (3, '취소'),
                                                          (4, '변경');


DROP TABLE IF EXISTS bookings;
CREATE TABLE bookings (
                          booking_no bigint NOT NULL COMMENT '예약 번호, autoincrement',
                          mb_no bigint NOT NULL COMMENT '예약자 번호',
                          room_no bigint NOT NULL COMMENT '회의실 번호',
                          change_no bigint NOT NULL COMMENT '특이사항',
                          booking_code varchar(10) NOT NULL COMMENT '예약 코드',
                          booking_date timestamp NOT NULL COMMENT '예약 사작일자',
                          attendee_count int NOT NULL COMMENT '예약 인원',
                          finishes_at timestamp NULL COMMENT '예약 종료일자',
                          created_at timestamp NOT NULL COMMENT '예약 생성일자',

                          CONSTRAINT  pk_bookings PRIMARY KEY (booking_no),
                          CONSTRAINT qk_bookings UNIQUE (booking_code),
                          CONSTRAINT fj_booking_member FOREIGN KEY (mb_no) REFERENCES members(mb_no),
                          CONSTRAINT fk_bookings_room FOREIGN KEY (room_no) REFERENCES rooms(room_no),
                          CONSTRAINT fk_booking_change FOREIGN KEY (change_no) REFERENCES booking_chages(change_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
