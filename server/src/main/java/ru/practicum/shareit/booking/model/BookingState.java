package ru.practicum.shareit.booking.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BookingState {
    ALL("ALL"),
    PAST("PAST"),
    CURRENT("CURRENT"),
    WAITING("WAITING"),
    REJECTED("REJECTED"),
    FUTURE("FUTURE");
    private final String state;
}