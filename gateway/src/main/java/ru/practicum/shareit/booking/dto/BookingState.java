package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Optional;

@Getter
@AllArgsConstructor
public enum BookingState {
    ALL("ALL"),
    PAST("PAST"),
    CURRENT("CURRENT"),
    WAITING("WAITING"),
    REJECTED("REJECTED"),
    FUTURE("FUTURE");
    private final String name;

    public static Optional<BookingState> isBookingState(String stringState) {
        for (BookingState state : values()) {
            if (state.name().equalsIgnoreCase(stringState)) {
                return Optional.of(state);
            }
        }
        return Optional.empty();
    }
}
