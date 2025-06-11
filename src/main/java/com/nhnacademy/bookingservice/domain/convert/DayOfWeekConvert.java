package com.nhnacademy.bookingservice.domain.convert;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Convert;

import java.time.DayOfWeek;

@Convert
public class DayOfWeekConvert implements AttributeConverter<DayOfWeek, Integer> {
    @Override
    public Integer convertToDatabaseColumn(DayOfWeek attribute) {
        if(attribute == null) {
            return null;
        }
        return attribute.getValue();
    }

    @Override
    public DayOfWeek convertToEntityAttribute(Integer dbData) {
        if(dbData == null) {
            return null;
        }
        return DayOfWeek.of(dbData);
    }
}
