DROP TABLE IF EXISTS bookings;
DROP TABLE IF EXISTS meeting_rooms;
DROP TABLE IF EXISTS booking_changes;

CREATE TABLE meeting_rooms (
                               meeting_room_no bigint NOT NULL COMMENT '회의실 번호, autoincrement',
                               meeting_room_name varchar(10) NOT NULL COMMENT '회의실 이름',
                               meeting_room_capacity int NOT NULL COMMENT '수용 인원',
                               PRIMARY KEY (meeting_room_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE booking_changes (
                                 change_no bigint NOT NULL COMMENT '예약 특이사항 번호, autoincrement',
                                 change_name varchar(10) NOT NULL COMMENT '특이사항 이름',
                                 PRIMARY KEY (change_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE bookings (
                          booking_no bigint NOT NULL COMMENT '예약 번호, autoincrement',
                          mb_no bigint NOT NULL COMMENT '예약자 번호',
                          meeting_room_no bigint NOT NULL COMMENT '회의실 번호',
                          change_no bigint DEFAULT NULL COMMENT '특이사항',
                          booking_code varchar(10) NOT NULL COMMENT '예약 코드',
                          attendee_count int NOT NULL COMMENT '예약 인원',
                          booking_date timestamp NOT NULL COMMENT '예약 시작일자',
                          finishes_at timestamp DEFAULT NULL COMMENT '예약 종료일자',
                          created_at timestamp NOT NULL COMMENT '예약 생성일자',
                          PRIMARY KEY (booking_no),
                          UNIQUE KEY booking_code (booking_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


ALTER TABLE meeting_rooms
    MODIFY meeting_room_no bigint NOT NULL AUTO_INCREMENT COMMENT '회의실 번호, autoincrement';

ALTER TABLE booking_changes
    MODIFY change_no bigint NOT NULL AUTO_INCREMENT COMMENT '예약 특이사항 번호, autoincrement';

ALTER TABLE bookings
    MODIFY booking_no bigint NOT NULL AUTO_INCREMENT COMMENT '예약 번호, autoincrement';

ALTER TABLE bookings
    ADD CONSTRAINT fk_bookings_members FOREIGN KEY (mb_no) REFERENCES members(mb_no),
	ADD CONSTRAINT fk_bookings_rooms FOREIGN KEY (meeting_room_no) REFERENCES meeting_rooms(meeting_room_no),
	ADD CONSTRAINT fk_bookings_changes FOREIGN KEY (change_no) REFERENCES booking_changes(change_no);

INSERT INTO booking_changes(change_name) VALUES
                                             ('연장'),
                                             ('종료'),
                                             ('취소'),
                                             ('변경');

INSERT INTO meeting_rooms (meeting_room_name, meeting_room_capacity) VALUES
                                                                         ('회의실A', 10),
                                                                         ('회의실B', 20),
                                                                         ('회의실C', 15),
                                                                         ('회의실D', 8),
                                                                         ('회의실E', 25);