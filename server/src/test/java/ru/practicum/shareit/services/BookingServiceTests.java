package ru.practicum.shareit.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.dto.BookingStartDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.errors.exceptions.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.pagination.Pagination;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTests {
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private ItemRepository itemRepository;
    @InjectMocks
    private BookingServiceImpl bookingService;
    private Item item;
    private User user;
    private Booking booking;
    private BookingStartDto bookingStartDto;

    @BeforeEach
    void beforeEach() {
        user = User.builder()
                .id(1L)
                .name("Михаил")
                .email("mishka@ya.ru")
                .build();

        Request request = Request.builder()
                .id(1L)
                .user(user)
                .description("Помогите найти нужный велосипед")
                .build();

        item = Item.builder()
                .available(true)
                .request(request)
                .owner(user)
                .build();

        booking = Booking.builder()
                .item(item)
                .booker(user)
                .status(Status.WAITING)
                .build();

        bookingStartDto = BookingStartDto.builder()
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(Status.WAITING.getStatus())
                .bookerId(1L)
                .itemId(1L)
                .build();
    }

    @Test
    void ifTryToCreateItemWhichDoesNotExistThenItemNotFoundException() {
        Mockito.when(itemRepository.findById(bookingStartDto.getItemId()))
                .thenThrow(new ItemNotFoundException("Вещи с таким id не существует"));

        final ItemNotFoundException exception = assertThrows(
                ItemNotFoundException.class,
                () -> bookingService.create(1L, bookingStartDto));

        assertEquals("Вещи с таким id не существует", exception.getMessage());
        Mockito.verify(itemRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
        Mockito.verify(userRepository, Mockito.never())
                .findById(Mockito.anyLong());
        Mockito.verify(bookingRepository, Mockito.never())
                .save(Mockito.any());
    }

    @Test
    void ifCreateAndUserDoesNotExistsThenUserNotFoundException() {
        Mockito.when(itemRepository.findById(bookingStartDto.getItemId()))
                .thenReturn(Optional.of(item));
        Mockito.when(userRepository.findById(1L))
                .thenThrow(new UserNotFoundException("Пользователь с таким id не найден"));

        final UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> bookingService.create(1L, bookingStartDto));

        assertEquals("Пользователь с таким id не найден", exception.getMessage());
        Mockito.verify(itemRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
        Mockito.verify(userRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
        Mockito.verify(bookingRepository, Mockito.never())
                .save(Mockito.any());
    }

    @Test
    void ifItemToBookBelongsToUserThenItemNotFoundException() {
        Mockito.when(itemRepository.findById(bookingStartDto.getItemId()))
                .thenReturn(Optional.of(item));
        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        final ItemNotFoundException exception = assertThrows(
                ItemNotFoundException.class,
                () -> bookingService.create(1L, bookingStartDto));
        Assertions.assertEquals("Пользователь не может забронировать свою вещь", exception.getMessage());

        Mockito.verify(itemRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
        Mockito.verify(userRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
        Mockito.verify(bookingRepository, Mockito.never())
                .save(Mockito.any());
    }

    @Test
    void ifTryToBookNotAvailableItemThenBadRequestException() {
        Item notAvailable = Item.builder()
                .id(5L)
                .owner(user)
                .available(false)
                .build();

        BookingStartDto bookingStartDto = BookingStartDto.builder()
                .itemId(5L)
                .build();

        Mockito.when(itemRepository.findById(5L))
                .thenReturn(Optional.of(notAvailable));

        final BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> bookingService.create(2L, bookingStartDto));

        Assertions.assertEquals("Вещь недоступна, поэтому бронирование невозможно", exception.getMessage());
        Mockito.verify(itemRepository, Mockito.times(1))
                .findById(5L);
        Mockito.verify(userRepository, Mockito.never())
                .findById(Mockito.anyLong());
        Mockito.verify(bookingRepository, Mockito.never())
                .save(Mockito.any());
    }

    @Test
    void ifUpdatingNotExistingBookingThenBookingNotFoundException() {
        Mockito.when(bookingRepository.findById(1L))
                .thenThrow(new BookingNotFoundException("Бронирования с таким id не найдено"));

        final BookingNotFoundException exception = assertThrows(
                BookingNotFoundException.class,
                () -> bookingService.update(1L, 1L, true));

        Assertions.assertEquals("Бронирования с таким id не найдено", exception.getMessage());
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
        Mockito.verify(bookingRepository, Mockito.never())
                .save(Mockito.any(Booking.class));
    }

    @Test
    void ifBookingToUpdateBelongsToAnotherPersonThenBookingNotFoundException() {
        Mockito.when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));

        final BookingNotFoundException exception = assertThrows(
                BookingNotFoundException.class,
                () -> bookingService.update(10L, 1L, true));

        Assertions.assertEquals("Бронирование с таким id не найдено", exception.getMessage());
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
        Mockito.verify(bookingRepository, Mockito.never())
                .save(Mockito.any(Booking.class));
    }

    @Test
    void ifUpdatingWithRejectedStatusThenBadRequestException() {
        Booking rejected = Booking.builder()
                .id(1L)
                .item(item)
                .status(Status.REJECTED)
                .build();

        Mockito.when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(rejected));

        final BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> bookingService.update(1L, 1L, true));

        Assertions.assertEquals("Нельзя изменить статус", exception.getMessage());
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
        Mockito.verify(bookingRepository, Mockito.never())
                .save(Mockito.any(Booking.class));
    }

    @Test
    void ifUpdatingWithApprovedStatusThenBadRequestException() {
        Booking approved = Booking.builder()
                .id(1L)
                .item(item)
                .status(Status.APPROVED)
                .build();

        Mockito.when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(approved));

        final BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> bookingService.update(1L, 1L, true));

        Assertions.assertEquals("Нельзя изменить статус", exception.getMessage());
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
        Mockito.verify(bookingRepository, Mockito.never())
                .save(Mockito.any(Booking.class));
    }

    @Test
    void ifBookingToUpdateIsOkThenCallSaveBookingRepository() {
        Mockito.when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));
        Mockito.when(bookingRepository.save(Mockito.any(Booking.class)))
                .thenReturn(booking);

        bookingService.update(1L, 1L, true);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
        Mockito.verify(bookingRepository, Mockito.times(1))
                .save(Mockito.any(Booking.class));
    }

    @Test
    void ifBookingIdDoesNotExistsThenBookingNotFoundException() {
        Mockito.when(bookingRepository.findById(1L))
                .thenThrow(new BookingNotFoundException("Бронирования с таким id не найдено"));

        final BookingNotFoundException exception = assertThrows(
                BookingNotFoundException.class,
                () -> bookingService.getById(1L, 1L));

        Assertions.assertEquals("Бронирования с таким id не найдено", exception.getMessage());
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
    }

    @Test
    void ifBookingIdBelongsToAnotherUserThenBookingNotFoundException() {
        Mockito.when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));

        final BookingNotFoundException exception = assertThrows(
                BookingNotFoundException.class,
                () -> bookingService.getById(10L, 1L));

        Assertions.assertEquals("Бронирование с таким id не найдено", exception.getMessage());
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
    }

    @Test
    void ifUserBookingFromIsIncorrectThenIllegalPaginationException() {
        Mockito.when(userRepository.existsById(1L))
                .thenReturn(true);

        final IllegalPaginationException exception = assertThrows(
                IllegalPaginationException.class,
                () -> bookingService.getUserBookings(1L, "ALL", -1, 10));

        Assertions.assertEquals("Переменная from должна быть больше, либо равна 0",
                exception.getMessage());
        Mockito.verify(userRepository, Mockito.times(1))
                .existsById(Mockito.anyLong());
    }

    @Test
    void ifUserBookingSizeIsIncorrectThenIllegalPaginationException() {
        Mockito.when(userRepository.existsById(1L))
                .thenReturn(true);

        final IllegalPaginationException exception = assertThrows(
                IllegalPaginationException.class,
                () -> bookingService.getUserBookings(1L, "ALL", 0, 0));

        Assertions.assertEquals("переменная size должна быть больше, либо равна 1",
                exception.getMessage());
        Mockito.verify(userRepository, Mockito.times(1))
                .existsById(Mockito.anyLong());
    }

    @Test
    void ifGetUserBookingsAndAllStateThenGetAllByBookerIdBookingRepository() {
        Pageable pageable = Pagination.of(0, 10, Sort.by("start").descending());
        Slice<Booking> bookings = new SliceImpl<>(List.of(booking));

        Mockito.when(userRepository.existsById(1L))
                .thenReturn(true);
        Mockito.when(bookingRepository.getAllByBookerId(1L, pageable))
                .thenReturn(bookings);

        bookingService.getUserBookings(1L, "ALL", 0, 10);
        Mockito.verify(userRepository, Mockito.times(1))
                .existsById(Mockito.anyLong());
        Mockito.verify(bookingRepository, Mockito.times(1))
                .getAllByBookerId(1L, pageable);
        Mockito.verify(bookingRepository, Mockito.never())
                .findAllByBookerId(Mockito.anyLong(), Mockito.any(LocalDateTime.class), Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .findAllByBookerIdAndStartAfter(Mockito.anyLong(), Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .findAllByBookerIdAndEndBefore(Mockito.anyLong(), Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .findAllByBookerIdAndStatus(Mockito.anyLong(), Mockito.any(), Mockito.any(Pageable.class));
    }

    @Test
    void ifTryToGetUserBookingsAndUserDoesNotExistsThenUserNotFoundException() {
        Mockito.when(userRepository.existsById(1L))
                .thenReturn(false);

        final UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> bookingService.getUserBookings(1L, "ALL", 0, 10));

        Assertions.assertEquals("Пользователь с таким id не найден", exception.getMessage());
        Mockito.verify(userRepository, Mockito.times(1))
                .existsById(Mockito.anyLong());
    }

    @Test
    void ifGetUserBookingsAndCurrentStateThenFindAllByBookerIdBookingRepository() {
        Slice<Booking> bookings = new SliceImpl<>(List.of(booking));
        Mockito.when(userRepository.existsById(1L))
                .thenReturn(true);
        Mockito.when(bookingRepository.findAllByBookerId(Mockito.anyLong(), Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class)))
                .thenReturn(bookings);

        bookingService.getUserBookings(1L, "CURRENT", 0, 10);
        Mockito.verify(userRepository, Mockito.times(1))
                .existsById(Mockito.anyLong());
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findAllByBookerId(Mockito.anyLong(), Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .getAllByBookerId(Mockito.anyLong(), Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .findAllByBookerIdAndStartAfter(Mockito.anyLong(), Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .findAllByBookerIdAndEndBefore(Mockito.anyLong(), Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .findAllByBookerIdAndStatus(Mockito.anyLong(), Mockito.any(), Mockito.any(Pageable.class));
    }

    @Test
    void ifGetUserBookingsAndFutureStateThenFindAllByBookerIdAndStartAfterBookingRepository() {
        Slice<Booking> bookings = new SliceImpl<>(List.of(booking));
        Mockito.when(userRepository.existsById(1L))
                .thenReturn(true);
        Mockito.when(bookingRepository.findAllByBookerIdAndStartAfter(Mockito.anyLong(),
                        Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class)))
                .thenReturn(bookings);

        bookingService.getUserBookings(1L, "FUTURE", 0, 10);

        Mockito.verify(userRepository, Mockito.times(1))
                .existsById(Mockito.anyLong());
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findAllByBookerIdAndStartAfter(Mockito.anyLong(), Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .getAllByBookerId(Mockito.anyLong(), Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .findAllByBookerId(Mockito.anyLong(), Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .findAllByBookerIdAndEndBefore(Mockito.anyLong(), Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .findAllByBookerIdAndStatus(Mockito.anyLong(), Mockito.any(), Mockito.any(Pageable.class));
    }

    @Test
    void ifGetUserBookingsAndWaitingStateThenFindAllByBookerIdAndStartAfterBookingRepository() {
        Pageable pageable = Pagination.of(0, 10, Sort.by("start").descending());
        Slice<Booking> bookings = new SliceImpl<>(List.of(booking));
        Mockito.when(userRepository.existsById(1L))
                .thenReturn(true);
        Mockito.when(bookingRepository.findAllByBookerIdAndStatus(1L, Status.WAITING, pageable))
                .thenReturn(bookings);

        bookingService.getUserBookings(1L, "WAITING", 0, 10);
        Mockito.verify(userRepository, Mockito.times(1))
                .existsById(Mockito.anyLong());
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findAllByBookerIdAndStatus(1L, Status.WAITING, pageable);
        Mockito.verify(bookingRepository, Mockito.never())
                .getAllByBookerId(Mockito.anyLong(), Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .findAllByBookerId(Mockito.anyLong(), Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .findAllByBookerIdAndStartAfter(Mockito.anyLong(), Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .findAllByBookerIdAndEndBefore(Mockito.anyLong(), Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class));
    }

    @Test
    void ifGetUserBookingsAndPastStateThenFindAllByBookerIdAndStartAfterBookingRepository() {
        Slice<Booking> bookings = new SliceImpl<>(List.of(booking));
        Mockito.when(userRepository.existsById(1L))
                .thenReturn(true);
        Mockito.when(bookingRepository.findAllByBookerIdAndEndBefore(Mockito.anyLong(),
                        Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class)))
                .thenReturn(bookings);

        bookingService.getUserBookings(1L, "PAST", 0, 10);
        Mockito.verify(userRepository, Mockito.times(1))
                .existsById(Mockito.anyLong());
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findAllByBookerIdAndEndBefore(Mockito.anyLong(), Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .getAllByBookerId(Mockito.anyLong(), Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .findAllByBookerId(Mockito.anyLong(), Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .findAllByBookerIdAndStartAfter(Mockito.anyLong(), Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .findAllByBookerIdAndStatus(Mockito.anyLong(), Mockito.any(), Mockito.any(Pageable.class));
    }

    @Test
    void ifGetUserBookingsAndRejectedStateThenFindAllByBookerIdAndStartAfterBookingRepository() {
        Pageable pageable = Pagination.of(0, 10, Sort.by("start").descending());
        Slice<Booking> bookings = new SliceImpl<>(List.of(booking));
        Mockito.when(userRepository.existsById(1L))
                .thenReturn(true);
        Mockito.when(bookingRepository.findAllByBookerIdAndStatus(1L, Status.REJECTED, pageable))
                .thenReturn(bookings);

        bookingService.getUserBookings(1L, "REJECTED", 0, 10);

        Mockito.verify(userRepository, Mockito.times(1))
                .existsById(Mockito.anyLong());
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findAllByBookerIdAndStatus(1L, Status.REJECTED, pageable);
        Mockito.verify(bookingRepository, Mockito.never())
                .getAllByBookerId(Mockito.anyLong(), Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .findAllByBookerId(Mockito.anyLong(), Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .findAllByBookerIdAndStartAfter(Mockito.anyLong(), Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .findAllByBookerIdAndEndBefore(Mockito.anyLong(), Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class));
    }

    @Test
    void ifUserBookingsFoundWithFromIsNotValidThenIllegalPaginationException() {
        Mockito.when(itemRepository.existsByOwnerId(1L))
                .thenReturn(true);

        final IllegalPaginationException exception = assertThrows(
                IllegalPaginationException.class,
                () -> bookingService.findBookingsByOwner(1L, "ALL", -1, 10));

        Assertions.assertEquals("Переменная from должна быть больше, либо равна 0",
                exception.getMessage());
        Mockito.verify(itemRepository, Mockito.times(1))
                .existsByOwnerId(Mockito.anyLong());
    }

    @Test
    void ifUserBookingsFoundWithIfSizeIsNotValidThenIllegalPaginationException() {
        Mockito.when(itemRepository.existsByOwnerId(1L))
                .thenReturn(true);

        final IllegalPaginationException exception = assertThrows(
                IllegalPaginationException.class,
                () -> bookingService.findBookingsByOwner(1L, "ALL", 0, 0));

        Assertions.assertEquals("переменная size должна быть больше, либо равна 1",
                exception.getMessage());
        Mockito.verify(itemRepository, Mockito.times(1))
                .existsByOwnerId(Mockito.anyLong());
    }

    @Test
    void ifUserBookingsFoundAndUserDoesNotExistsThenUserNotFoundException() {
        Mockito.when(itemRepository.existsByOwnerId(3L))
                .thenReturn(false);

        final UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> bookingService.findBookingsByOwner(3L, "ALL", 0, 10));

        Assertions.assertEquals("У этого пользователя нет доступных вещей", exception.getMessage());
        Mockito.verify(itemRepository, Mockito.times(1))
                .existsByOwnerId(Mockito.anyLong());
    }

    @Test
    void ifBookingsOfOwnerFoundWithCurrentStateThenFindAllByBookerIdBookingRepository() {
        Slice<Booking> bookings = new SliceImpl<>(List.of(booking));
        Mockito.when(itemRepository.existsByOwnerId(1L))
                .thenReturn(true);
        Mockito.when(bookingRepository.findAllCurrentByOwnerId(Mockito.anyLong(), Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class)))
                .thenReturn(bookings);

        bookingService.findBookingsByOwner(1L, "CURRENT", 0, 10);

        Mockito.verify(itemRepository, Mockito.times(1))
                .existsByOwnerId(Mockito.anyLong());
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findAllCurrentByOwnerId(Mockito.anyLong(), Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .findAllByOwnerId(Mockito.anyLong(), Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .findAllFutureByOwnerId(Mockito.anyLong(), Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .getAllPastByOwnerId(Mockito.anyLong(), Mockito.any(LocalDateTime.class), Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .findAllByOwnerIdAndStatus(Mockito.anyLong(), Mockito.any(), Mockito.any(Pageable.class));
    }

    @Test
    void ifBookingsOfOwnerFoundWithAllStateThenGetAllByBookerIdBookingRepository() {
        Pageable pageable = Pagination.of(0, 10, Sort.by("start").descending());
        Slice<Booking> bookings = new SliceImpl<>(List.of(booking));
        Mockito.when(itemRepository.existsByOwnerId(1L))
                .thenReturn(true);
        Mockito.when(bookingRepository.findAllByOwnerId(1L, pageable))
                .thenReturn(bookings);

        bookingService.findBookingsByOwner(1L, "ALL", 0, 10);
        Mockito.verify(itemRepository, Mockito.times(1))
                .existsByOwnerId(Mockito.anyLong());
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findAllByOwnerId(1L, pageable);
        Mockito.verify(bookingRepository, Mockito.never())
                .findAllCurrentByOwnerId(Mockito.anyLong(), Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .findAllFutureByOwnerId(Mockito.anyLong(), Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .getAllPastByOwnerId(Mockito.anyLong(), Mockito.any(LocalDateTime.class), Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .findAllByOwnerIdAndStatus(Mockito.anyLong(), Mockito.any(), Mockito.any(Pageable.class));
    }

    @Test
    void ifBookingsOfOwnerFoundWithFutureStateThenFindAllByBookerIdAndStartAfterBookingRepository() {
        Slice<Booking> bookings = new SliceImpl<>(List.of(booking));
        Mockito.when(itemRepository.existsByOwnerId(1L))
                .thenReturn(true);
        Mockito.when(bookingRepository.findAllFutureByOwnerId(Mockito.anyLong(), Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class)))
                .thenReturn(bookings);

        bookingService.findBookingsByOwner(1L, "FUTURE", 0, 10);
        Mockito.verify(itemRepository, Mockito.times(1))
                .existsByOwnerId(Mockito.anyLong());
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findAllFutureByOwnerId(Mockito.anyLong(), Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .findAllByOwnerId(Mockito.anyLong(), Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .findAllCurrentByOwnerId(Mockito.anyLong(), Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .getAllPastByOwnerId(Mockito.anyLong(), Mockito.any(LocalDateTime.class), Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .findAllByOwnerIdAndStatus(Mockito.anyLong(), Mockito.any(), Mockito.any(Pageable.class));
    }

    @Test
    void ifBookingsOfOwnerFoundWithPastStateThenFindAllByBookerIdAndStartAfterBookingRepository() {
        Slice<Booking> bookings = new SliceImpl<>(List.of(booking));
        Mockito.when(itemRepository.existsByOwnerId(1L))
                .thenReturn(true);
        Mockito.when(bookingRepository.getAllPastByOwnerId(Mockito.anyLong(), Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class)))
                .thenReturn(bookings);

        bookingService.findBookingsByOwner(1L, "PAST", 0, 10);
        Mockito.verify(itemRepository, Mockito.times(1))
                .existsByOwnerId(Mockito.anyLong());
        Mockito.verify(bookingRepository, Mockito.times(1))
                .getAllPastByOwnerId(Mockito.anyLong(), Mockito.any(LocalDateTime.class), Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .findAllByOwnerId(Mockito.anyLong(), Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .findAllCurrentByOwnerId(Mockito.anyLong(), Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .findAllFutureByOwnerId(Mockito.anyLong(), Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .findAllByOwnerIdAndStatus(Mockito.anyLong(), Mockito.any(), Mockito.any(Pageable.class));
    }
}
