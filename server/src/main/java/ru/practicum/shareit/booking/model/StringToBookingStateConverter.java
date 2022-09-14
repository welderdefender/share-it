package ru.practicum.shareit.booking.model;

import org.springframework.core.convert.converter.Converter;

import org.springframework.stereotype.Component;

import ru.practicum.shareit.errors.exceptions.BadRequestException;

@Component
public class StringToBookingStateConverter implements Converter<String, BookingState> {

    @Override
    public BookingState convert(String source) {
        try {
            return BookingState.valueOf(source.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(String.format("an unexpected error occurred when converting string " +
                    "value=%s into BookingState", source));
        }
    }
}
