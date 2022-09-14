package ru.practicum.shareit.booking.dto;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

public class BookingMapper {

    public static BookingDto toBookingDto(Booking booking) {
        return BookingDto.builder()
                .id(booking.getId())
                .bookerId(booking.getBooker().getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .build();
    }

    public static BookingFinishDto toBookingFinishDto(Booking booking) {
        return BookingFinishDto.builder()
                .id(booking.getId())
                .booker(booking.getBooker())
                .item(booking.getItem())
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus().getStatus())
                .build();
    }

    public static Booking toBooking(BookingStartDto bookingStartDto, User user, Item item) {
        return Booking.builder()
                .id(bookingStartDto.getId())
                .booker(user)
                .item(item)
                .start(bookingStartDto.getStart())
                .end(bookingStartDto.getEnd())
                .status(Status.valueOf(bookingStartDto.getStatus()))
                .build();
    }
}
