package ru.practicum.shareit.repositories;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Slice;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.pagination.Pagination;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class BookingRepositoryTests {
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private TestEntityManager tem;

    @Test
    void getAllByBookerId() throws InterruptedException {
        User owner = User.builder()
                .name("Юлия")
                .email("juliya@ya.ru")
                .build();
        User booker = User.builder()
                .name("арендатор")
                .email("booker@ya.ru")
                .build();
        Item itemOne = Item.builder()
                .name("Велосипед трехколесный")
                .owner(owner)
                .available(true)
                .description("женский")
                .build();
        Item itemTwo = Item.builder()
                .name("Велосипед складной")
                .owner(owner)
                .available(true)
                .description("Красный")
                .build();
        Booking bookingOne = Booking.builder()
                .booker(booker)
                .item(itemOne)
                .status(Status.WAITING)
                .start(LocalDateTime.now().plusSeconds(2))
                .end(LocalDateTime.now().plusSeconds(3))
                .build();
        Booking bookingTwo = Booking.builder()
                .booker(booker)
                .item(itemTwo)
                .status(Status.WAITING)
                .start(LocalDateTime.now().plusSeconds(2))
                .end(LocalDateTime.now().plusHours(7))
                .build();

        tem.persist(owner);
        tem.persist(booker);
        tem.persist(itemOne);
        tem.persist(itemTwo);
        tem.persist(bookingOne);
        tem.persist(bookingTwo);
        Thread.sleep(5000L);

        Slice<Booking> bookings = bookingRepository.findAllByBookerId(booker.getId(), LocalDateTime.now(),
                Pagination.of(0, 5));
        assertEquals(1, bookings.getContent().size());
        assertThat(bookings.getContent().get(0), equalTo(bookingTwo));
    }

    @Test
    void findAllByOwner() {
        User owner = User.builder()
                .name("Юлия")
                .email("juliya@ya.ru")
                .build();
        User ownerTwo = User.builder()
                .name("Полина")
                .email("polina@ya.ru")
                .build();
        User booker = User.builder()
                .name("арендатор")
                .email("booker@ya.ru")
                .build();
        Item itemOne = Item.builder()
                .name("Велосипед трехколесный")
                .owner(owner)
                .available(true)
                .description("женский")
                .build();
        Item itemTwo = Item.builder()
                .name("Велосипед складной")
                .owner(ownerTwo)
                .available(true)
                .description("Красный")
                .build();
        Booking bookingOne = Booking.builder()
                .booker(booker)
                .item(itemOne)
                .status(Status.WAITING)
                .start(LocalDateTime.now().plusHours(2))
                .end(LocalDateTime.now().plusHours(4))
                .build();
        Booking bookingTwo = Booking.builder()
                .booker(booker)
                .item(itemTwo)
                .status(Status.WAITING)
                .start(LocalDateTime.now().plusHours(5))
                .end(LocalDateTime.now().plusHours(7))
                .build();

        tem.persist(owner);
        tem.persist(ownerTwo);
        tem.persist(booker);
        tem.persist(itemOne);
        tem.persist(itemTwo);
        tem.persist(bookingOne);
        tem.persist(bookingTwo);
        Slice<Booking> bookingsList = bookingRepository.findAllByOwnerId(owner.getId(), Pagination.of(0, 5));
        assertEquals(1, bookingsList.getContent().size());
        assertThat(bookingsList.getContent().get(0), equalTo(bookingOne));
    }

    @Test
    void getAllPreviousByOwner() throws InterruptedException {
        User owner = User.builder()
                .name("Юлия")
                .email("juliya@ya.ru")
                .build();
        User booker = User.builder()
                .name("арендатор")
                .email("booker@ya.ru")
                .build();
        Item itemOne = Item.builder()
                .name("Велосипед трехколесный")
                .owner(owner)
                .available(true)
                .description("женский")
                .build();
        Item itemTwo = Item.builder()
                .name("Велосипед складной")
                .owner(owner)
                .available(true)
                .description("Красный")
                .build();
        Booking bookingOne = Booking.builder()
                .booker(booker)
                .item(itemOne)
                .status(Status.WAITING)
                .start(LocalDateTime.now().plusSeconds(2))
                .end(LocalDateTime.now().plusSeconds(3))
                .build();
        Booking bookingTwo = Booking.builder()
                .booker(booker)
                .item(itemTwo)
                .status(Status.WAITING)
                .start(LocalDateTime.now().plusSeconds(2))
                .end(LocalDateTime.now().plusSeconds(3))
                .build();

        tem.persist(owner);
        tem.persist(booker);
        tem.persist(itemOne);
        tem.persist(itemTwo);
        tem.persist(bookingOne);
        tem.persist(bookingTwo);
        Thread.sleep(5000L);
        Slice<Booking> bookingsList = bookingRepository.getAllPastByOwnerId(owner.getId(), LocalDateTime.now(),
                Pagination.of(0, 5));
        assertEquals(2, bookingsList.getContent().size());
        assertThat(bookingsList.getContent().get(0), equalTo(bookingOne));
        assertThat(bookingsList.getContent().get(1), equalTo(bookingTwo));
    }

    @Test
    void findAllCurrentByOwner() throws InterruptedException {
        User owner = User.builder()
                .name("Юлия")
                .email("juliya@ya.ru")
                .build();
        User booker = User.builder()
                .name("арендатор")
                .email("booker@ya.ru")
                .build();
        Item itemOne = Item.builder()
                .name("Велосипед трехколесный")
                .owner(owner)
                .available(true)
                .description("женский")
                .build();
        Item itemTwo = Item.builder()
                .name("Велосипед складной")
                .owner(owner)
                .available(true)
                .description("Красный")
                .build();
        Booking bookingOne = Booking.builder()
                .booker(booker)
                .item(itemOne)
                .status(Status.WAITING)
                .start(LocalDateTime.now().plusSeconds(2))
                .end(LocalDateTime.now().plusSeconds(3))
                .build();
        Booking bookingTwo = Booking.builder()
                .booker(booker)
                .item(itemTwo)
                .status(Status.WAITING)
                .start(LocalDateTime.now().plusSeconds(2))
                .end(LocalDateTime.now().plusHours(7))
                .build();

        tem.persist(owner);
        tem.persist(booker);
        tem.persist(itemOne);
        tem.persist(itemTwo);
        tem.persist(bookingOne);
        tem.persist(bookingTwo);
        Thread.sleep(3000L);
        Slice<Booking> bookings = bookingRepository.findAllCurrentByOwnerId(owner.getId(), LocalDateTime.now(),
                Pagination.of(0, 5));
        assertEquals(1, bookings.getContent().size());
        assertThat(bookings.getContent().get(0), equalTo(bookingTwo));
    }

    @Test
    void findAllNextByOwner() {
        User owner = User.builder()
                .name("Юлия")
                .email("juliya@ya.ru")
                .build();
        User booker = User.builder()
                .name("арендатор")
                .email("booker@ya.ru")
                .build();
        Item itemOne = Item.builder()
                .name("Велосипед трехколесный")
                .owner(owner)
                .available(true)
                .description("женский")
                .build();
        Item itemTwo = Item.builder()
                .name("Велосипед складной")
                .owner(owner)
                .available(true)
                .description("Красный")
                .build();
        Booking bookingOne = Booking.builder()
                .booker(booker)
                .item(itemOne)
                .status(Status.WAITING)
                .start(LocalDateTime.now().plusHours(3))
                .end(LocalDateTime.now().plusHours(7))
                .build();
        Booking bookingTwo = Booking.builder()
                .booker(booker)
                .item(itemTwo)
                .status(Status.WAITING)
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .build();

        tem.persist(owner);
        tem.persist(booker);
        tem.persist(itemOne);
        tem.persist(itemTwo);
        tem.persist(bookingOne);
        tem.persist(bookingTwo);
        Slice<Booking> bookingsList = bookingRepository.findAllFutureByOwnerId(owner.getId(), LocalDateTime.now(),
                Pagination.of(0, 5));
        assertEquals(2, bookingsList.getContent().size());
        assertThat(bookingsList.getContent().get(0), equalTo(bookingOne));
        assertThat(bookingsList.getContent().get(1), equalTo(bookingTwo));
    }

    @Test
    void findAllByOwnerAndStatus() {
        User owner = User.builder()
                .name("Юлия")
                .email("juliya@ya.ru")
                .build();
        User booker = User.builder()
                .name("арендатор")
                .email("booker@ya.ru")
                .build();
        Item itemOne = Item.builder()
                .name("Велосипед трехколесный")
                .owner(owner)
                .available(true)
                .description("женский")
                .build();
        Item itemTwo = Item.builder()
                .name("Велосипед складной")
                .owner(owner)
                .available(true)
                .description("Красный")
                .build();
        Booking bookingOne = Booking.builder()
                .booker(booker)
                .item(itemOne)
                .status(Status.REJECTED)
                .start(LocalDateTime.now().plusHours(3))
                .end(LocalDateTime.now().plusHours(7))
                .build();
        Booking bookingTwo = Booking.builder()
                .booker(booker)
                .item(itemTwo)
                .status(Status.WAITING)
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .build();

        tem.persist(owner);
        tem.persist(booker);
        tem.persist(itemOne);
        tem.persist(itemTwo);
        tem.persist(bookingOne);
        tem.persist(bookingTwo);
        Slice<Booking> bookingsList = bookingRepository.findAllByOwnerIdAndStatus(owner.getId(), Status.REJECTED,
                Pagination.of(0, 5));
        assertEquals(1, bookingsList.getContent().size());
        assertThat(bookingsList.getContent().get(0), equalTo(bookingOne));
    }
}
