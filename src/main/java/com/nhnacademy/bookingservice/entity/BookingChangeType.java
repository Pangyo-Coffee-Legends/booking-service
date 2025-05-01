package com.nhnacademy.bookingservice.entity;

public enum BookingChangeType {
    EXTEND(1L, "연장"),
    FINISH(2L, "종료"),
    CANCEL(3L, "취소"),
    CHANGE(4L, "변경");

    private final Long id;
    private final String name;

    BookingChangeType(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static BookingChangeType fromId(Long id) {
        for (BookingChangeType type : values()) {
            if (type.getId().equals(id)) {
                return type;
            }
        }
        throw new IllegalArgumentException("유효하지 않는 BookingChangeType id 입니다: " + id);
    }

    public static BookingChangeType fromName(String name) {
        for (BookingChangeType type : values()) {
            if (type.getName().equalsIgnoreCase(name)) {
                return type;
            }
        }
        throw new IllegalArgumentException("유효하지 않는 BookingChangeType name 입니다: " + name);
    }
}
