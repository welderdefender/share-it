package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingFinishDto;
import ru.practicum.shareit.booking.dto.BookingStartDto;
import ru.practicum.shareit.booking.service.BookingService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingService bookingService;

    @Autowired
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public BookingFinishDto create(@RequestHeader(value = "X-Sharer-User-Id") long userId,
                                   @Valid @RequestBody BookingStartDto bookingStartDto) {
        return bookingService.create(userId, bookingStartDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingFinishDto update(@RequestHeader("X-Sharer-User-Id") long userId, @PathVariable long bookingId,
                                   @RequestParam(value = "approved") boolean isApproved) {
        return bookingService.update(userId, bookingId, isApproved);
    }

    @GetMapping("/{bookingId}")
    public BookingFinishDto getById(@RequestHeader("X-Sharer-User-Id") long userId, @PathVariable long bookingId) {
        return bookingService.getById(userId, bookingId);
    }

    @GetMapping("/owner")
    public List<BookingFinishDto> findBookingsByOwner(@RequestHeader("X-Sharer-User-Id") long userId,
                                                      @RequestParam(value = "state", required = false, defaultValue =
                                                              "ALL")
                                                              String state) {
        return bookingService.findBookingsByOwner(userId, state);
    }

    @GetMapping
    public List<BookingFinishDto> getUserBookings(@RequestHeader("X-Sharer-User-Id") long userId,
                                                  @RequestParam(value = "state", required = false, defaultValue = "ALL")
                                                          String state) {
        return bookingService.getUserBookings(userId, state);
    }
}
