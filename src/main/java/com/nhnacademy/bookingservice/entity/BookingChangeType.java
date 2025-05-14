package com.nhnacademy.bookingservice.entity;


/**
 * 예약 변경 유형을 나타내는 열거형입니다.
 * <p>
 * 이 열거형은 예약의 변경 상태를 나타내기 위해 사용됩니다.
 * 예: 연장, 종료, 취소, 변경
 * </p>
 */
public enum BookingChangeType {

    /**
     * 예약 연장
     */
    EXTEND(1L, "연장"),

    /**
     * 예약 종료
     */
    FINISH(2L, "종료"),

    /**
     * 예약 취소
     */
    CANCEL(3L, "취소"),

    /**
     * 예약 정보 변경
     */
    CHANGE(4L, "변경");

    private final Long id;
    private final String name;

    /**
     * BookingChangeType 생성자
     *
     * @param id   예약 변경 유형의 고유 ID
     * @param name 예약 변경 유형의 이름 (예: 연장, 종료 등)
     */
    BookingChangeType(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * 예약 변경 유형의 ID를 반환합니다.
     *
     * @return 예약 변경 유형 ID
     */
    public Long getId() {
        return id;
    }


    /**
     * 예약 변경 유형의 이름을 반환합니다.
     *
     * @return 예약 변경 유형 이름
     */
    public String getName() {
        return name;
    }


    /**
     * ID를 기반으로 해당 BookingChangeType을 반환합니다.
     *
     * @param id 조회할 예약 변경 유형 ID
     * @return 해당 ID에 대응되는 BookingChangeType
     * @throws IllegalArgumentException 유효하지 않은 ID일 경우 예외 발생
     */
    public static BookingChangeType fromId(Long id) {
        for (BookingChangeType type : values()) {
            if (type.getId().equals(id)) {
                return type;
            }
        }
        throw new IllegalArgumentException("유효하지 않는 BookingChangeType id 입니다: " + id);
    }


    /**
     * 이름을 기반으로 해당 BookingChangeType을 반환합니다.
     *
     * @param name 조회할 예약 변경 유형 이름
     * @return 해당 이름에 대응되는 BookingChangeType
     * @throws IllegalArgumentException 유효하지 않은 이름일 경우 예외 발생
     */
    public static BookingChangeType fromName(String name) {
        for (BookingChangeType type : values()) {
            if (type.getName().equalsIgnoreCase(name)) {
                return type;
            }
        }
        throw new IllegalArgumentException("유효하지 않는 BookingChangeType name 입니다: " + name);
    }
}
