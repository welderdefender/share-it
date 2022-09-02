package ru.practicum.shareit.booking.service;

import org.springframework.data.domain.*;
import ru.practicum.shareit.pagination.Pagination;
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
    public List<BookingFinishDto> findBookingsByOwner(long ownerId, String state, int from, int size) {
        checkState(state);
        if (!itemRepository.existsByOwnerId(ownerId))
            throw new UserNotFoundException("У этого пользователя нет доступных вещей");

        Pageable sortedByStartDesc = Pagination.of(from, size, Sort.by("start").descending());
        return getFilteredBookingsByStateAndOwnerId(ownerId, sortedByStartDesc, State.valueOf(state)).get()
                .map(BookingMapper::toBookingFinishDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingFinishDto> getUserBookings(long userId, String state, int from, int size) {
        checkState(state);
        if (!userRepository.existsById(userId))
            throw new UserNotFoundException("Пользователь с таким id не найден");
        Pageable sortedByStartDesc = Pagination.of(from, size, Sort.by("start").descending());
        return getFilteredBookingsByStateAndBookerId(userId, sortedByStartDesc, state).get()
                .map(BookingMapper::toBookingFinishDto)
                .collect(Collectors.toList());
    }

    private Slice<Booking> getFilteredBookingsByStateAndOwnerId(long ownerId, Pageable pageable, State state) {
        switch (state.getState()) {
            case "ALL":
                return bookingRepository.findAllByOwnerId(ownerId, pageable);
            case "CURRENT":
                return bookingRepository.findAllCurrentByOwnerId(ownerId, LocalDateTime.now(), pageable);
            case "FUTURE":
                return bookingRepository.findAllFutureByOwnerId(ownerId, LocalDateTime.now(), pageable);
            case "PAST":
                return bookingRepository.getAllPastByOwnerId(ownerId, LocalDateTime.now(), pageable);
            default:
                return bookingRepository.findAllByOwnerIdAndStatus(ownerId, Status.valueOf(state.getState()), pageable);
        }
    }

    private void checkState(String state) {
        if (Arrays.stream(State.values()).noneMatch(element -> Objects.equals(element.getState(), state)))
            throw new BadRequestException("Unknown state: UNSUPPORTED_STATUS");
    }

    private Slice<Booking> getFilteredBookingsByStateAndBookerId(long bookerId, Pageable pageable, String state) {
        switch (state) {
            case "ALL":
                return bookingRepository.getAllByBookerId(bookerId, pageable);
            case "CURRENT":
                return bookingRepository.findAllByBookerId(bookerId, LocalDateTime.now(), pageable);
            case "FUTURE":
                return bookingRepository.findAllByBookerIdAndStartAfter(bookerId, LocalDateTime.now(), pageable);
            case "PAST":
                return bookingRepository.findAllByBookerIdAndEndBefore(bookerId, LocalDateTime.now(), pageable);
            default:
                return bookingRepository.findAllByBookerIdAndStatus(bookerId, Status.valueOf(state), pageable);
        }
    }
}