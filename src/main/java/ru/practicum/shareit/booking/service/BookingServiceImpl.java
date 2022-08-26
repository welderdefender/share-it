package ru.practicum.shareit.booking.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingFinishDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingStartDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.errors.exceptions.BadRequestException;
import ru.practicum.shareit.errors.exceptions.BookingNotFoundException;
import ru.practicum.shareit.errors.exceptions.ItemNotFoundException;
import ru.practicum.shareit.errors.exceptions.UserNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    public BookingServiceImpl(BookingRepository bookingRepository, UserRepository userRepository,
                              ItemRepository itemRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
    }

    @Override
    public BookingFinishDto create(long userId, BookingStartDto bookingStartDto) {
        Item item = itemRepository.findById(bookingStartDto.getItemId())
                .orElseThrow(() -> new ItemNotFoundException("Вещь с таким id не найдена"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с таким id не найден"));
        LocalDateTime start = bookingStartDto.getStart();
        LocalDateTime end = bookingStartDto.getEnd();
        if (userId == item.getOwner().getId())
            throw new ItemNotFoundException("Пользователь попытался забронировать свою вещь");
        if (!item.getAvailable() || start.isBefore(LocalDateTime.now()) || start.isAfter(end))
            throw new BadRequestException("Забронировать вещь не удалось из-за некорректных временных рамок");

        bookingStartDto.setStatus(Status.WAITING.getStatus());
        return BookingMapper.toBookingFinishDto(bookingRepository.save(BookingMapper.toBooking(bookingStartDto, user,
                item)));
    }

    @Override
    public BookingFinishDto update(long userId, long bookingId, boolean isApproved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Бронирование с таким id не найдено"));

        if (userId != booking.getItem().getOwner().getId())
            throw new BookingNotFoundException("Бронирование с таким id не найдено");
        if (booking.getStatus() == Status.APPROVED || booking.getStatus() == Status.REJECTED)
            throw new BadRequestException("Нельзя изменить статус");

        booking.setStatus(isApproved ? Status.APPROVED : Status.REJECTED);
        return BookingMapper.toBookingFinishDto(bookingRepository.save(booking));
    }

    @Override
    public BookingFinishDto getById(long userId, long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Бронирование с таким id не найдено"));

        if (userId != booking.getItem().getOwner().getId() && userId != booking.getBooker().getId())
            throw new BookingNotFoundException("Бронирование с таким id не найдено");

        return BookingMapper.toBookingFinishDto(booking);
    }

    @Override
    public List<BookingFinishDto> findBookingsByOwner(long ownerId, String state) {
        checkState(state);

        if (!itemRepository.existsByOwnerId(ownerId))
            throw new UserNotFoundException("У этого пользователя нет доступных вещей");

        return getFilteredBookingsByState(bookingRepository.findBookingsByOwnerId(ownerId), State.valueOf(state));
    }

    @Override
    public List<BookingFinishDto> getUserBookings(long userId, String state) {
        checkState(state);
        if (!userRepository.existsById(userId))
            throw new UserNotFoundException("Пользователь с таким id не найден");
        return getFilteredBookingsByState(bookingRepository.getBookingsByBookerIdOrderByStartDesc(userId),
                State.valueOf(state));
    }

    private List<BookingFinishDto> getFilteredBookingsByState(List<Booking> bookings, State state) {
        List<BookingFinishDto> bookingsDto = bookings.stream()
                .map(BookingMapper::toBookingFinishDto)
                .collect(Collectors.toList());

        if (state == State.ALL) return bookingsDto;

        return bookingsDto.stream()
                .filter(bookingFinishDto -> filterByState(bookingFinishDto, state))
                .collect(Collectors.toList());
    }

    private boolean filterByState(BookingFinishDto bookingFinishDto, State state) {
        switch (state.getState()) {
            case "CURRENT":
                return bookingFinishDto.getStart().isBefore(LocalDateTime.now())
                        && bookingFinishDto.getEnd().isAfter(LocalDateTime.now());
            case "PAST":
                return bookingFinishDto.getEnd().isBefore(LocalDateTime.now());
            case "FUTURE":
                return bookingFinishDto.getStart().isAfter(LocalDateTime.now());
            default:
                return Objects.equals(bookingFinishDto.getStatus(), state.getState());
        }
    }

    private void checkState(String state) {
        if (Arrays.stream(State.values()).noneMatch(element -> Objects.equals(element.getState(), state)))
            throw new BadRequestException("Unknown state: UNSUPPORTED_STATUS");
    }
}