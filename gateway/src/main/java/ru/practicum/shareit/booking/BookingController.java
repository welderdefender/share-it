package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.errors.BadRequestException;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
@Controller

public class BookingController {
    private final BookingClient bookingClient;

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader("X-Sharer-User-Id") long userId,
                                         @RequestBody @Valid BookingDto bookingDto) {
        log.info("Бронирование {} создано пользователем {}", bookingDto, userId);
        return bookingClient.create(userId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> update(@RequestHeader("X-Sharer-User-Id") long userId,
                                         @PathVariable @Positive long bookingId,
                                         @RequestParam(value = "approved") boolean isApproved) {
        log.info("Бронирование {} обновлено пользователем {}, статус {}", bookingId, userId, isApproved);
        return bookingClient.update(userId, bookingId, isApproved);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> findById(@RequestHeader("X-Sharer-User-Id") long userId,
                                           @PathVariable @Positive Long bookingId) {
        log.info("Получение бронирования {} от пользователя {}", bookingId, userId);
        return bookingClient.findById(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> getUserBookings(@RequestHeader("X-Sharer-User-Id") long userId,
                                                  @RequestParam(name = "state", defaultValue = "ALL", required =
                                                          false) String stateParam,
                                                  @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") int from,
                                                  @Positive @RequestParam(name = "size", defaultValue = "10") int size) {
        BookingState state = BookingState.isBookingState(stateParam)
                .orElseThrow(() -> new BadRequestException("Unknown state: " + stateParam));
        log.info("Список бронирований пользователя с параметрами state={}, userId={}, from={}, size={}", stateParam,
                userId, from, size);
        return bookingClient.getUserBookings(userId, state, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getBookingsByOwner(@RequestHeader("X-Sharer-User-Id") long ownerId,
                                                     @RequestParam(name = "state", defaultValue = "ALL", required =
                                                             false) String stateParam,
                                                     @PositiveOrZero @RequestParam(defaultValue = "0", required =
                                                             false) int from,
                                                     @Positive @RequestParam(defaultValue = "10", required = false) int size) {
        log.info("Получение бронирований владельца с id={}, где state={}, from={}, size={}", ownerId, stateParam,
                from, size);
        BookingState state = BookingState.isBookingState(stateParam)
                .orElseThrow(() -> new BadRequestException("Unknown state: " + stateParam));

        return bookingClient.findBookingsByOwner(ownerId, state, from, size);
    }
}
