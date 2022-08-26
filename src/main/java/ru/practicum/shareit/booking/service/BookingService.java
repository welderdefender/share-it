package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingFinishDto;
import ru.practicum.shareit.booking.dto.BookingStartDto;

import java.util.List;

public interface BookingService {
    BookingFinishDto create(long userId, BookingStartDto bookingStartDto);

    BookingFinishDto update(long userId, long bookingId, boolean approved);

    BookingFinishDto getById(long userId, long bookingId);

    List<BookingFinishDto> findBookingsByOwner(long ownerId, String state);

    List<BookingFinishDto> getUserBookings(long userId, String state);
}
