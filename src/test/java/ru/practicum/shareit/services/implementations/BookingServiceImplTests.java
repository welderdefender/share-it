package ru.practicum.shareit.services.implementations;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingFinishDto;
import ru.practicum.shareit.booking.dto.BookingStartDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.service.UserService;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingServiceImplTests {
    private final EntityManager tem;
    private static UserDto userDtoItemOwner;
    private static UserDto userDtoBooker;
    private static ItemDto itemDto;
    private final ItemService itemService;
    private final UserService userService;
    private final BookingService bookingService;
    private static BookingStartDto bookingStartDto;

    @BeforeAll
    public static void beforeAll() {
        userDtoItemOwner = UserDto.builder()
                .email("test@ya.ru")
                .name("Тестировщик")
                .build();
        userDtoBooker = UserDto.builder()
                .name("арендатор")
                .email("booker@ya.ru")
                .build();
        itemDto = ItemDto.builder()
                .name("Велосипед")
                .description("Велописед двухместный")
                .available(true)
                .build();
        bookingStartDto = BookingStartDto.builder()
                .start(LocalDateTime.now().plusHours(1L))
                .end(LocalDateTime.now().plusDays(2L))
                .build();
    }

    @Test
    void createBooking() {
        UserDto owner = userService.create(userDtoItemOwner);
        ItemDto item = itemService.create(owner.getId(), itemDto);
        UserDto booker = userService.create(userDtoBooker);
        bookingStartDto.setItemId(item.getId());
        BookingFinishDto returned = bookingService.create(booker.getId(), bookingStartDto);
        TypedQuery<Booking> query = tem.createQuery("Select b from Booking b where b.id = :bookingId", Booking.class);

        Booking booking = query
                .setParameter("bookingId", returned.getId())
                .getSingleResult();

        assertThat(returned.getId(), notNullValue());
        assertThat(returned.getStatus(), equalTo(booking.getStatus().name()));
        assertThat(returned.getItem(), equalTo(ItemMapper.toItem(item, UserMapper.toUser(owner))));
        assertThat(returned.getBooker(), equalTo(UserMapper.toUser(booker)));
    }

    @Test
    void updateStatus() {
        UserDto owner = userService.create(userDtoItemOwner);
        ItemDto item = itemService.create(owner.getId(), itemDto);
        UserDto booker = userService.create(userDtoBooker);
        bookingStartDto.setItemId(item.getId());
        BookingFinishDto bookingToReturn = bookingService.create(booker.getId(), bookingStartDto);
        BookingFinishDto bookingToUpdate = bookingService.update(owner.getId(), bookingToReturn.getId(), true);
        TypedQuery<Booking> query = tem.createQuery("Select b from Booking b where b.id = :bookingId", Booking.class);

        Booking booking = query
                .setParameter("bookingId", bookingToReturn.getId())
                .getSingleResult();

        assertThat(bookingToUpdate.getId(), equalTo(bookingToReturn.getId()));
        assertThat(bookingToUpdate.getStatus(), equalTo(Status.APPROVED.name()));
        assertThat(booking.getStatus(), equalTo(Status.APPROVED));
        assertThat(bookingToUpdate.getItem(), equalTo(ItemMapper.toItem(item, UserMapper.toUser(owner))));
        assertThat(bookingToUpdate.getBooker(), equalTo(UserMapper.toUser(booker)));
    }

    @Test
    void findById() {
        UserDto owner = userService.create(userDtoItemOwner);
        ItemDto item = itemService.create(owner.getId(), itemDto);
        UserDto booker = userService.create(userDtoBooker);
        bookingStartDto.setItemId(item.getId());
        BookingFinishDto bookingToReturn = bookingService.create(booker.getId(), bookingStartDto);
        BookingFinishDto foundBooking = bookingService.getById(booker.getId(), bookingToReturn.getId());
        TypedQuery<Booking> query = tem.createQuery("Select b from Booking b where b.id = :bookingId", Booking.class);

        Booking booking = query
                .setParameter("bookingId", bookingToReturn.getId())
                .getSingleResult();

        assertThat(foundBooking.getStatus(), equalTo(booking.getStatus().name()));
        assertThat(foundBooking.getBooker(), equalTo(booking.getBooker()));
        assertThat(foundBooking.getItem(), equalTo(booking.getItem()));
        assertThat(foundBooking.getStart(), equalTo(booking.getStart()));
        assertThat(foundBooking.getEnd(), equalTo(booking.getEnd()));
    }

    @Test
    void getUserBookingsCurrent() throws InterruptedException {
        UserDto itemOwner = userService.create(userDtoItemOwner);
        ItemDto item = itemService.create(itemOwner.getId(), itemDto);
        UserDto booker = userService.create(userDtoBooker);
        bookingStartDto.setItemId(item.getId());
        UserDto anotherBooker = UserDto.builder()
                .name("Юрий")
                .email("yuri@ya.ru")
                .build();
        UserDto returnedBooker = userService.create(anotherBooker);
        BookingStartDto bookingTwo = BookingStartDto.builder()
                .itemId(item.getId())
                .bookerId(returnedBooker.getId())
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .build();
        BookingStartDto currentBooking = BookingStartDto.builder()
                .itemId(item.getId())
                .bookerId(booker.getId())
                .start(LocalDateTime.now().plusSeconds(1))
                .end(LocalDateTime.now().plusDays(4))
                .build();

        bookingService.create(booker.getId(), bookingStartDto);
        bookingService.create(returnedBooker.getId(), bookingTwo);
        BookingFinishDto returnedCurrent = bookingService.create(booker.getId(), currentBooking);
        Thread.sleep(5000L);
        List<BookingFinishDto> bookerBookingsList = bookingService.getUserBookings(booker.getId(), "CURRENT", 0, 10);
        assertThat(bookerBookingsList.size(), equalTo(1));
        assertThat(bookerBookingsList.get(0).getBooker(), equalTo(UserMapper.toUser(booker)));
        assertThat(bookerBookingsList.get(0), equalTo(returnedCurrent));
    }

    @Test
    void getBookingsByOwnerAll() {
        UserDto owner = userService.create(userDtoItemOwner);
        ItemDto item = itemService.create(owner.getId(), itemDto);
        UserDto booker = userService.create(userDtoBooker);
        bookingStartDto.setItemId(item.getId());
        UserDto bookerTwo = UserDto.builder()
                .name("Юрий")
                .email("yuri@ya.ru")
                .build();
        UserDto returnedAnotherBooker = userService.create(bookerTwo);
        BookingStartDto bookingTwo = BookingStartDto.builder()
                .itemId(item.getId())
                .bookerId(returnedAnotherBooker.getId())
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .build();
        BookingStartDto bookingOfBooker = BookingStartDto.builder()
                .itemId(item.getId())
                .bookerId(booker.getId())
                .start(LocalDateTime.now().plusDays(3))
                .end(LocalDateTime.now().plusDays(4))
                .build();

        BookingFinishDto first = bookingService.create(booker.getId(), bookingStartDto);
        BookingFinishDto second = bookingService.create(returnedAnotherBooker.getId(), bookingTwo);
        BookingFinishDto third = bookingService.create(booker.getId(), bookingOfBooker);
        List<BookingFinishDto> bookerBookingsList = bookingService.findBookingsByOwner(owner.getId(), "ALL", 0, 10);
        assertThat(bookerBookingsList.size(), equalTo(3));
        assertThat(bookerBookingsList.get(0).getItem().getOwner(), equalTo(UserMapper.toUser(owner)));
        assertThat(bookerBookingsList.get(0), equalTo(third));
        assertThat(bookerBookingsList.get(1).getItem().getOwner(), equalTo(UserMapper.toUser(owner)));
        assertThat(bookerBookingsList.get(1), equalTo(second));
        assertThat(bookerBookingsList.get(2).getItem().getOwner(), equalTo(UserMapper.toUser(owner)));
        assertThat(bookerBookingsList.get(2), equalTo(first));
    }

    @Test
    void getCurrentBookingsByOwner() throws InterruptedException {
        UserDto owner = userService.create(userDtoItemOwner);
        ItemDto item = itemService.create(owner.getId(), itemDto);
        UserDto booker = userService.create(userDtoBooker);
        bookingStartDto.setItemId(item.getId());
        UserDto bookerTwo = UserDto.builder()
                .name("Юрий")
                .email("yuri@ya.ru")
                .build();
        UserDto returnedBooker = userService.create(bookerTwo);
        BookingStartDto anotherBooking = BookingStartDto.builder()
                .itemId(item.getId())
                .bookerId(returnedBooker.getId())
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .build();
        BookingStartDto currentDto = BookingStartDto.builder()
                .itemId(item.getId())
                .bookerId(booker.getId())
                .start(LocalDateTime.now().plusSeconds(1))
                .end(LocalDateTime.now().plusDays(4))
                .build();

        bookingService.create(booker.getId(), bookingStartDto);
        bookingService.create(returnedBooker.getId(), anotherBooking);
        BookingFinishDto returnedCurrent = bookingService.create(booker.getId(), currentDto);
        Thread.sleep(5000L);
        List<BookingFinishDto> bookerBookingsList = bookingService.findBookingsByOwner(owner.getId(), "CURRENT", 0, 10);
        assertThat(bookerBookingsList.size(), equalTo(1));
        assertThat(bookerBookingsList.get(0).getItem().getOwner(), equalTo(UserMapper.toUser(owner)));
        assertThat(bookerBookingsList.get(0), equalTo(returnedCurrent));
    }

    @Test
    void getNextBookingsByOwner() {
        UserDto owner = userService.create(userDtoItemOwner);
        ItemDto item = itemService.create(owner.getId(), itemDto);
        UserDto booker = userService.create(userDtoBooker);
        bookingStartDto.setItemId(item.getId());
        UserDto bookerTwo = UserDto.builder()
                .name("Юрий")
                .email("yuri@ya.ru")
                .build();
        UserDto returnedBooker = userService.create(bookerTwo);
        BookingStartDto anotherBooking = BookingStartDto.builder()
                .itemId(item.getId())
                .bookerId(returnedBooker.getId())
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .build();
        BookingStartDto anotherNextBooking = BookingStartDto.builder()
                .itemId(item.getId())
                .bookerId(booker.getId())
                .start(LocalDateTime.now().plusDays(3))
                .end(LocalDateTime.now().plusDays(4))
                .build();

        BookingFinishDto firstBooking = bookingService.create(booker.getId(), bookingStartDto);
        BookingFinishDto secondBooking = bookingService.create(returnedBooker.getId(), anotherBooking);
        BookingFinishDto thirdBooking = bookingService.create(booker.getId(), anotherNextBooking);
        List<BookingFinishDto> bookerBookingsList = bookingService.findBookingsByOwner(owner.getId(), "FUTURE", 0, 10);
        assertThat(bookerBookingsList.size(), equalTo(3));
        assertThat(bookerBookingsList.get(0).getItem().getOwner(), equalTo(UserMapper.toUser(owner)));
        assertThat(bookerBookingsList.get(0), equalTo(thirdBooking));
        assertThat(bookerBookingsList.get(1).getItem().getOwner(), equalTo(UserMapper.toUser(owner)));
        assertThat(bookerBookingsList.get(1), equalTo(secondBooking));
        assertThat(bookerBookingsList.get(2).getItem().getOwner(), equalTo(UserMapper.toUser(owner)));
        assertThat(bookerBookingsList.get(2), equalTo(firstBooking));
    }

    @Test
    void getPreviousBookingsByOwner() throws InterruptedException {
        UserDto owner = userService.create(userDtoItemOwner);
        ItemDto item = itemService.create(owner.getId(), itemDto);
        UserDto booker = userService.create(userDtoBooker);
        bookingStartDto.setItemId(item.getId());
        UserDto bookerTwo = UserDto.builder()
                .name("Юрий")
                .email("yuri@ya.ru")
                .build();
        UserDto returnedBooker = userService.create(bookerTwo);
        BookingStartDto anotherBooking = BookingStartDto.builder()
                .itemId(item.getId())
                .bookerId(returnedBooker.getId())
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .build();
        BookingStartDto previousDto = BookingStartDto.builder()
                .itemId(item.getId())
                .bookerId(booker.getId())
                .start(LocalDateTime.now().plusSeconds(1))
                .end(LocalDateTime.now().plusSeconds(2))
                .build();

        bookingService.create(booker.getId(), bookingStartDto);
        bookingService.create(returnedBooker.getId(), anotherBooking);
        BookingFinishDto returnedPast = bookingService.create(booker.getId(), previousDto);
        Thread.sleep(5000L);
        List<BookingFinishDto> bookerBookingsList = bookingService.findBookingsByOwner(owner.getId(), "PAST", 0, 10);
        assertThat(bookerBookingsList.size(), equalTo(1));
        assertThat(bookerBookingsList.get(0).getItem().getOwner(), equalTo(UserMapper.toUser(owner)));
        assertThat(bookerBookingsList.get(0), equalTo(returnedPast));
    }

    @Test
    void getWaitingBookingsByOwner() {
        UserDto owner = userService.create(userDtoItemOwner);
        ItemDto item = itemService.create(owner.getId(), itemDto);
        UserDto booker = userService.create(userDtoBooker);
        bookingStartDto.setItemId(item.getId());
        UserDto bookerTwo = UserDto.builder()
                .name("Юрий")
                .email("yuri@ya.ru")
                .build();
        UserDto returnedAnotherBooker = userService.create(bookerTwo);
        BookingStartDto bookingTwo = BookingStartDto.builder()
                .itemId(item.getId())
                .bookerId(returnedAnotherBooker.getId())
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .build();
        BookingStartDto bookingByBooker = BookingStartDto.builder()
                .itemId(item.getId())
                .bookerId(booker.getId())
                .start(LocalDateTime.now().plusDays(3))
                .end(LocalDateTime.now().plusDays(4))
                .build();

        BookingFinishDto firstBooking = bookingService.create(booker.getId(), bookingStartDto);
        BookingFinishDto secondBooking = bookingService.create(returnedAnotherBooker.getId(), bookingTwo);
        BookingFinishDto thirdBooking = bookingService.create(booker.getId(), bookingByBooker);
        bookingService.update(owner.getId(), firstBooking.getId(), true);
        bookingService.update(owner.getId(), thirdBooking.getId(), true);
        List<BookingFinishDto> bookerBookingsList = bookingService.findBookingsByOwner(owner.getId(), "WAITING", 0, 10);
        assertThat(bookerBookingsList.size(), equalTo(1));
        assertThat(bookerBookingsList.get(0).getItem().getOwner(), equalTo(UserMapper.toUser(owner)));
        assertThat(bookerBookingsList.get(0), equalTo(secondBooking));
        assertThat(bookerBookingsList.get(0).getStatus(), equalTo(Status.WAITING.name()));
    }

    @Test
    void getDeclinedBookingsByOwner() {
        UserDto owner = userService.create(userDtoItemOwner);
        ItemDto item = itemService.create(owner.getId(), itemDto);
        UserDto booker = userService.create(userDtoBooker);
        bookingStartDto.setItemId(item.getId());
        UserDto bookerTwo = UserDto.builder()
                .name("Юрий")
                .email("yuri@ya.ru")
                .build();
        UserDto returnedBooker = userService.create(bookerTwo);
        BookingStartDto anotherBooking = BookingStartDto.builder()
                .itemId(item.getId())
                .bookerId(returnedBooker.getId())
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .build();
        BookingStartDto bookingByBooker = BookingStartDto.builder()
                .itemId(item.getId())
                .bookerId(booker.getId())
                .start(LocalDateTime.now().plusDays(3))
                .end(LocalDateTime.now().plusDays(4))
                .build();

        bookingService.create(booker.getId(), bookingStartDto);
        bookingService.create(returnedBooker.getId(), anotherBooking);
        BookingFinishDto second = bookingService.create(booker.getId(), bookingByBooker);
        BookingFinishDto updatedToDecline = bookingService.update(owner.getId(), second.getId(), false);
        List<BookingFinishDto> bookerBookingsList = bookingService.findBookingsByOwner(owner.getId(), "REJECTED", 0,
                10);
        assertThat(bookerBookingsList.size(), equalTo(1));
        assertThat(bookerBookingsList.get(0).getItem().getOwner(), equalTo(UserMapper.toUser(owner)));
        assertThat(bookerBookingsList.get(0), equalTo(updatedToDecline));
        assertThat(bookerBookingsList.get(0).getStatus(), equalTo(Status.REJECTED.name()));
    }

    @Test
    void getAllUserBookings() {
        UserDto owner = userService.create(userDtoItemOwner);
        ItemDto item = itemService.create(owner.getId(), itemDto);
        UserDto booker = userService.create(userDtoBooker);
        bookingStartDto.setItemId(item.getId());
        UserDto bookerTwo = UserDto.builder()
                .name("Евгений")
                .email("zheka@ya.ru")
                .build();
        UserDto bookerTwoReturned = userService.create(bookerTwo);
        BookingStartDto booking = BookingStartDto.builder()
                .itemId(item.getId())
                .bookerId(bookerTwoReturned.getId())
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .build();
        BookingStartDto bookingOfBooker = BookingStartDto.builder()
                .itemId(item.getId())
                .bookerId(booker.getId())
                .start(LocalDateTime.now().plusDays(3))
                .end(LocalDateTime.now().plusDays(4))
                .build();

        BookingFinishDto bookingTwo = bookingService.create(booker.getId(), bookingStartDto);
        bookingService.create(bookerTwoReturned.getId(), booking);
        BookingFinishDto bookingThree = bookingService.create(booker.getId(), bookingOfBooker);
        List<BookingFinishDto> bookerBookingsList = bookingService.getUserBookings(booker.getId(), "ALL", 0, 10);
        assertThat(bookerBookingsList.size(), equalTo(2));
        assertThat(bookerBookingsList.get(0).getBooker(), equalTo(UserMapper.toUser(booker)));
        assertThat(bookerBookingsList.get(0), equalTo(bookingThree));
        assertThat(bookerBookingsList.get(1).getBooker(), equalTo(UserMapper.toUser(booker)));
        assertThat(bookerBookingsList.get(1), equalTo(bookingTwo));
    }

    @Test
    void getNextUserBookings() {
        UserDto owner = userService.create(userDtoItemOwner);
        ItemDto item = itemService.create(owner.getId(), itemDto);
        UserDto booker = userService.create(userDtoBooker);
        bookingStartDto.setItemId(item.getId());
        UserDto anotherBooker = UserDto.builder()
                .name("Юрий")
                .email("yuri@ya.ru")
                .build();
        UserDto returnedBooker = userService.create(anotherBooker);
        BookingStartDto bookingTwo = BookingStartDto.builder()
                .itemId(item.getId())
                .bookerId(returnedBooker.getId())
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .build();
        BookingStartDto bookingThree = BookingStartDto.builder()
                .itemId(item.getId())
                .bookerId(booker.getId())
                .start(LocalDateTime.now().plusDays(3))
                .end(LocalDateTime.now().plusDays(4))
                .build();

        BookingFinishDto nextBooking = bookingService.create(booker.getId(), bookingStartDto);
        bookingService.create(returnedBooker.getId(), bookingTwo);
        BookingFinishDto nextBookingTwo = bookingService.create(booker.getId(), bookingThree);
        List<BookingFinishDto> bookerBookingsList = bookingService.getUserBookings(booker.getId(), "FUTURE", 0, 10);
        assertThat(bookerBookingsList.size(), equalTo(2));
        assertThat(bookerBookingsList.get(0).getBooker(), equalTo(UserMapper.toUser(booker)));
        assertThat(bookerBookingsList.get(0), equalTo(nextBookingTwo));
        assertThat(bookerBookingsList.get(1).getBooker(), equalTo(UserMapper.toUser(booker)));
        assertThat(bookerBookingsList.get(1), equalTo(nextBooking));
    }

    @Test
    void getPreviousUserBookings() throws InterruptedException {
        UserDto owner = userService.create(userDtoItemOwner);
        ItemDto item = itemService.create(owner.getId(), itemDto);
        UserDto booker = userService.create(userDtoBooker);
        bookingStartDto.setItemId(item.getId());
        UserDto bookerTwo = UserDto.builder()
                .name("Юрий")
                .email("yuri@ya.ru")
                .build();
        UserDto returnedBooker = userService.create(bookerTwo);
        BookingStartDto anotherBooking = BookingStartDto.builder()
                .itemId(item.getId())
                .bookerId(returnedBooker.getId())
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .build();
        BookingStartDto prevDto = BookingStartDto.builder()
                .itemId(item.getId())
                .bookerId(booker.getId())
                .start(LocalDateTime.now().plusSeconds(1))
                .end(LocalDateTime.now().plusSeconds(2))
                .build();

        bookingService.create(booker.getId(), bookingStartDto);
        bookingService.create(returnedBooker.getId(), anotherBooking);
        BookingFinishDto returnedPast = bookingService.create(booker.getId(), prevDto);
        Thread.sleep(5000L);
        List<BookingFinishDto> bookerBookingsList = bookingService.getUserBookings(booker.getId(), "PAST", 0, 10);
        assertThat(bookerBookingsList.size(), equalTo(1));
        assertThat(bookerBookingsList.get(0).getBooker(), equalTo(UserMapper.toUser(booker)));
        assertThat(bookerBookingsList.get(0), equalTo(returnedPast));
    }

    @Test
    void getUserBookingsDeclined() {
        UserDto owner = userService.create(userDtoItemOwner);
        ItemDto item = itemService.create(owner.getId(), itemDto);
        UserDto booker = userService.create(userDtoBooker);
        bookingStartDto.setItemId(item.getId());
        UserDto bookerTwo = UserDto.builder()
                .name("Юрий")
                .email("yuri@ya.ru")
                .build();
        UserDto returnedBooker = userService.create(bookerTwo);
        BookingStartDto bookingTwo = BookingStartDto.builder()
                .itemId(item.getId())
                .bookerId(returnedBooker.getId())
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .build();
        BookingStartDto bookingOfBooker = BookingStartDto.builder()
                .itemId(item.getId())
                .bookerId(booker.getId())
                .start(LocalDateTime.now().plusDays(3))
                .end(LocalDateTime.now().plusDays(4))
                .build();

        BookingFinishDto first = bookingService.create(booker.getId(), bookingStartDto);
        bookingService.create(returnedBooker.getId(), bookingTwo);
        BookingFinishDto second = bookingService.create(booker.getId(), bookingOfBooker);
        BookingFinishDto updatedToRejected = bookingService.update(owner.getId(), second.getId(), false);
        List<BookingFinishDto> bookerBookings = bookingService.getUserBookings(booker.getId(), "REJECTED", 0, 10);
        assertThat(bookerBookings.size(), equalTo(1));
        assertThat(bookerBookings.get(0).getBooker(), equalTo(UserMapper.toUser(booker)));
        assertThat(bookerBookings.get(0), equalTo(updatedToRejected));
        assertThat(bookerBookings.get(0).getStatus(), equalTo(Status.REJECTED.name()));
    }

    @Test
    void getUserBookingsWithWaitingState() {
        UserDto owner = userService.create(userDtoItemOwner);
        ItemDto item = itemService.create(owner.getId(), itemDto);
        UserDto booker = userService.create(userDtoBooker);
        bookingStartDto.setItemId(item.getId());
        UserDto bookerTwo = UserDto.builder()
                .name("Юрий")
                .email("yuri@ya.ru")
                .build();
        UserDto returnedBooker = userService.create(bookerTwo);
        BookingStartDto anotherBooking = BookingStartDto.builder()
                .itemId(item.getId())
                .bookerId(returnedBooker.getId())
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .build();
        BookingStartDto bookingOfBooker = BookingStartDto.builder()
                .itemId(item.getId())
                .bookerId(booker.getId())
                .start(LocalDateTime.now().plusDays(3))
                .end(LocalDateTime.now().plusDays(4))
                .build();

        BookingFinishDto first = bookingService.create(booker.getId(), bookingStartDto);
        bookingService.create(returnedBooker.getId(), anotherBooking);
        BookingFinishDto second = bookingService.create(booker.getId(), bookingOfBooker);
        bookingService.update(owner.getId(), second.getId(), true);
        List<BookingFinishDto> bookerBookingsList = bookingService.getUserBookings(booker.getId(), "WAITING", 0, 10);
        assertThat(bookerBookingsList.size(), equalTo(1));
        assertThat(bookerBookingsList.get(0).getBooker(), equalTo(UserMapper.toUser(booker)));
        assertThat(bookerBookingsList.get(0), equalTo(first));
        assertThat(bookerBookingsList.get(0).getStatus(), equalTo(Status.WAITING.name()));
    }
}
