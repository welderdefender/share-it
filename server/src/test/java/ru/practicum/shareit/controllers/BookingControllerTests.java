package ru.practicum.shareit.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.dto.BookingStartDto;
import ru.practicum.shareit.errors.ErrorHandler;
import ru.practicum.shareit.errors.exceptions.BadRequestException;
import ru.practicum.shareit.errors.exceptions.BookingNotFoundException;
import ru.practicum.shareit.errors.exceptions.ItemNotFoundException;
import ru.practicum.shareit.errors.exceptions.UserNotFoundException;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.liquibase.enabled=false",
        "spring.flyway.enabled=false"
})

@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@Sql({"/schema.sql"})
class BookingControllerTests {
    @Autowired
    private BookingController bookingController;
    @Autowired
    private ItemController itemController;
    @Autowired
    private UserController userController;
    private static UserDto owner;
    private static ItemDto itemDto;
    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    public static void beforeAll() {
        owner = UserDto.builder()
                .email("test@ya.ru")
                .name("Тестировщик")
                .build();

        itemDto = ItemDto.builder()
                .owner(1L)
                .available(true)
                .name("велосипед")
                .description("горный скоростной")
                .build();
    }

    @BeforeEach
    void beforeEach() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(itemController, userController, bookingController)
                .setControllerAdvice(new ErrorHandler())
                .build();
        mapper.registerModule(new JavaTimeModule());
    }

    @Test
    void addBooking() throws Exception {
        createUser(owner);
        createItem(itemDto);

        UserDto booker = UserDto.builder()
                .name("арендатор")
                .email("bookertest@ya.ru")
                .build();
        createUser(booker);

        BookingStartDto bookingStartDto = BookingStartDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusSeconds(2))
                .end(LocalDateTime.now().plusSeconds(3))
                .build();

        mockMvc.perform(post("/bookings")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 2L)
                        .content(mapper.writeValueAsString(bookingStartDto)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("item.id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("item.name").value("велосипед"))
                .andExpect(MockMvcResultMatchers.jsonPath("item.description").value("горный скоростной"))
                .andExpect(MockMvcResultMatchers.jsonPath("item.available").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("booker.id").value("2"))
                .andExpect(MockMvcResultMatchers.jsonPath("booker.name").value("арендатор"))
                .andExpect(MockMvcResultMatchers.jsonPath("booker.email").value("bookertest@ya.ru"))
                .andExpect(MockMvcResultMatchers.jsonPath("status").value("WAITING"));
    }

    @Test
    void addBookingByNotExistsUserAndStatusIsNotFound() throws Exception {
        createUser(owner);
        createItem(itemDto);
        BookingStartDto bookingStartDto = BookingStartDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusSeconds(2))
                .end(LocalDateTime.now().plusSeconds(3))
                .build();

        mockMvc.perform(post("/bookings")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 5L)
                        .content(mapper.writeValueAsString(bookingStartDto)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof UserNotFoundException))
                .andExpect(result -> assertEquals("Пользователь с таким id не найден",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()))
                .andExpect(status().isNotFound());
    }

    @Test
    void addBookingByNotFoundOwnerAndStatusIsNotFound() throws Exception {
        createUser(owner);
        createItem(itemDto);
        BookingStartDto bookingStartDto = BookingStartDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusHours(2))
                .end(LocalDateTime.now().plusHours(3))
                .build();

        mapper.registerModule(new JavaTimeModule());

        mockMvc.perform(post("/bookings")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(bookingStartDto)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ItemNotFoundException))
                .andExpect(result -> assertEquals("Пользователь не может забронировать свою вещь",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()))
                .andExpect(status().isNotFound());
    }

    @Test
    void addBookingOfItemDoesNotExistsAndStatusIsNotFound() throws Exception {
        createUser(owner);
        BookingStartDto bookingStartDto = BookingStartDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusSeconds(2))
                .end(LocalDateTime.now().plusSeconds(3))
                .build();

        mockMvc.perform(post("/bookings")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(bookingStartDto)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ItemNotFoundException))
                .andExpect(result -> assertEquals("Вещь с таким id не найдена",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()))
                .andExpect(status().isNotFound());
    }

    @Test
    void addBookingWhenItemNotAvailableAndStatusIsBadRequest() throws Exception {
        createUser(owner);
        ItemDto item = ItemDto.builder()
                .name("велосипед")
                .description("детский")
                .available(false)
                .build();
        createItem(item);

        UserDto user = UserDto.builder()
                .name("арендатор")
                .email("bookertest@ya.ru")
                .build();
        createUser(user);

        BookingStartDto bookingStartDto = BookingStartDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusSeconds(2))
                .end(LocalDateTime.now().plusSeconds(3))
                .build();

        mockMvc.perform(post("/bookings")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 2L)
                        .content(mapper.writeValueAsString(bookingStartDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateStatusWhenBookingNotExistsAndStatusIsNotFound() throws Exception {
        mockMvc.perform(patch("/bookings/1?approved=true")
                        .header("X-Sharer-User-Id", 3L))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof BookingNotFoundException))
                .andExpect(result -> assertEquals("Бронирование с таким id не найдено",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()))
                .andExpect(status().isNotFound());
    }

    @Test
    void update() throws Exception {
        createUser(owner);
        createItem(itemDto);
        UserDto booker = UserDto.builder()
                .name("арендатор")
                .email("bookertest@ya.ru")
                .build();
        createUser(booker);

        BookingStartDto bookingStartDto = BookingStartDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusSeconds(2))
                .end(LocalDateTime.now().plusSeconds(3))
                .build();
        postBooking(bookingStartDto, 2L);

        mockMvc.perform(patch("/bookings/1?approved=true")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("status").value("APPROVED"));
    }

    @Test
    void findBookingByIdAndStatusIsOk() throws Exception {
        createUser(owner);
        createItem(itemDto);

        UserDto booker = UserDto.builder()
                .name("арендатор")
                .email("bookertest@ya.ru")
                .build();
        createUser(booker);

        BookingStartDto bookingStartDto = BookingStartDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusHours(2))
                .end(LocalDateTime.now().plusDays(3))
                .build();
        postBooking(bookingStartDto, 2L);

        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 2L))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("item.id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("item.name").value("велосипед"))
                .andExpect(MockMvcResultMatchers.jsonPath("item.description").value("горный скоростной"))
                .andExpect(MockMvcResultMatchers.jsonPath("item.available").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("booker.id").value("2"))
                .andExpect(MockMvcResultMatchers.jsonPath("booker.name").value("арендатор"))
                .andExpect(MockMvcResultMatchers.jsonPath("booker.email").value("bookertest@ya.ru"))
                .andExpect(MockMvcResultMatchers.jsonPath("start").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("end").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("status").value("WAITING"));
    }

    @Test
    void updateAlreadyApprovedStatusIsBadRequest() throws Exception {
        createUser(owner);
        createItem(itemDto);

        UserDto booker = UserDto.builder()
                .name("арендатор")
                .email("bookertest@ya.ru")
                .build();
        createUser(booker);

        BookingStartDto bookingStartDto = BookingStartDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusHours(2))
                .end(LocalDateTime.now().plusHours(3))
                .build();
        postBooking(bookingStartDto, 2L);

        mockMvc.perform(patch("/bookings/1?approved=true")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("status").value("APPROVED"));

        mockMvc.perform(patch("/bookings/1?approved=false")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof BadRequestException))
                .andExpect(result -> assertEquals("Нельзя изменить статус",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getDoesNotExistsBookingByIdAndStatusIsOk() throws Exception {
        createUser(owner);
        createItem(itemDto);

        UserDto booker = UserDto.builder()
                .name("арендатор")
                .email("bookertest@ya.ru")
                .build();
        createUser(booker);

        BookingStartDto bookingStartDto = BookingStartDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusHours(2))
                .end(LocalDateTime.now().plusDays(3))
                .build();
        postBooking(bookingStartDto, 2L);

        mockMvc.perform(get("/bookings/1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 2L)
                        .content(mapper.writeValueAsString(bookingStartDto)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("item.id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("item.name").value("велосипед"))
                .andExpect(MockMvcResultMatchers.jsonPath("item.description").value("горный скоростной"))
                .andExpect(MockMvcResultMatchers.jsonPath("item.available").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("booker.id").value("2"))
                .andExpect(MockMvcResultMatchers.jsonPath("booker.name").value("арендатор"))
                .andExpect(MockMvcResultMatchers.jsonPath("booker.email").value("bookertest@ya.ru"))
                .andExpect(MockMvcResultMatchers.jsonPath("start").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("end").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("status").value("WAITING"));
    }

    @Test
    void updateStatusWhenUsersBookingsNotExistsAndStatusIsNotFound() throws Exception {
        createUser(owner);
        createItem(itemDto);

        UserDto booker = UserDto.builder()
                .name("арендатор")
                .email("bookertest@ya.ru")
                .build();
        createUser(booker);

        BookingStartDto bookingStartDto = BookingStartDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusSeconds(2))
                .end(LocalDateTime.now().plusSeconds(3))
                .build();
        postBooking(bookingStartDto, 2L);

        mockMvc.perform(patch("/bookings/1?approved=true")
                        .header("X-Sharer-User-Id", 3L))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof BookingNotFoundException))
                .andExpect(result -> assertEquals("Бронирование с таким id не найдено",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()))
                .andExpect(status().isNotFound());
    }

    @Test
    void findBookingNoExistsAndStatusIsNotFound() throws Exception {
        createUser(owner);

        mockMvc.perform(get("/bookings/2")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof BookingNotFoundException))
                .andExpect(result -> assertEquals("Бронирование с таким id не найдено",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()))
                .andExpect(status().isNotFound());
    }

    @Test
    void findBookingByIdWithNoOwnerAndNoBookerAndStatusIsNotFound() throws Exception {
        createUser(owner);
        createItem(itemDto);
        UserDto booker = UserDto.builder()
                .name("арендатор")
                .email("bookertest@ya.ru")
                .build();
        createUser(booker);

        BookingStartDto bookingStartDto = BookingStartDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusHours(2))
                .end(LocalDateTime.now().plusDays(3))
                .build();
        postBooking(bookingStartDto, 2L);

        UserDto randomGuy = UserDto.builder()
                .name("Максим")
                .email("max@ya.ru")
                .build();
        createUser(randomGuy);

        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 3L))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof BookingNotFoundException))
                .andExpect(result -> assertEquals("Бронирование с таким id не найдено",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getBookingsByOwner() throws Exception {
        createUser(owner);
        createItem(itemDto);
        UserDto bookerOne = UserDto.builder()
                .name("арендатор")
                .email("bookertest@ya.ru")
                .build();
        createUser(bookerOne);
        UserDto bookerTwo = UserDto.builder()
                .name("Максим")
                .email("max@ya.ru")
                .build();
        createUser(bookerTwo);

        BookingStartDto bookingOne = BookingStartDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusHours(2))
                .end(LocalDateTime.now().plusDays(1))
                .build();
        postBooking(bookingOne, 2L);
        BookingStartDto bookingTwo = BookingStartDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .build();
        postBooking(bookingTwo, 2L);

        mockMvc.perform(patch("/bookings/2?approved=false")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());

        BookingStartDto bookingOfBookerTwo = BookingStartDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(5))
                .end(LocalDateTime.now().plusDays(6))
                .build();
        postBooking(bookingOfBookerTwo, 3L);

        mockMvc.perform(patch("/bookings/3?approved=true")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());

        mockMvc.perform(get("/bookings/owner?state=ALL&from=0&size=10")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(3))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value("3"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].item.id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].booker.id").value("3"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].status").value("APPROVED"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].id").value("2"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].item.id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].booker.id").value("2"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].status").value("REJECTED"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].item.id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].booker.id").value("2"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].status").value("WAITING"));
    }

    @Test
    void getUserBookingsByNotExistsUserStatusIsNotFound() throws Exception {
        createUser(owner);
        createItem(itemDto);

        UserDto booker = UserDto.builder()
                .name("арендатор")
                .email("bookertest@ya.ru")
                .build();
        createUser(booker);

        BookingStartDto bookingOne = BookingStartDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusHours(2))
                .end(LocalDateTime.now().plusDays(1))
                .build();
        postBooking(bookingOne, 2L);
        BookingStartDto bookingTwo = BookingStartDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .build();
        postBooking(bookingTwo, 2L);

        mockMvc.perform(get("/bookings?state=ALL&from=0&size=10")
                        .header("X-Sharer-User-Id", 5L))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof UserNotFoundException))
                .andExpect(result -> assertEquals("Пользователь с таким id не найден",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getNotOwnerBookingsAndStatusIsNotFound() throws Exception {
        createUser(owner);
        createItem(itemDto);

        UserDto booker = UserDto.builder()
                .name("арендатор")
                .email("bookertest@ya.ru")
                .build();
        createUser(booker);
        UserDto notOwner = UserDto.builder()
                .name("Максим")
                .email("max@ya.ru")
                .build();
        createUser(notOwner);

        BookingStartDto bookingOne = BookingStartDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusHours(2))
                .end(LocalDateTime.now().plusDays(1))
                .build();
        postBooking(bookingOne, 2L);
        BookingStartDto bookingTwo = BookingStartDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .build();
        postBooking(bookingTwo, 2L);

        mockMvc.perform(get("/bookings/owner?state=ALL&from=0&size=10")
                        .header("X-Sharer-User-Id", 3L))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof UserNotFoundException))
                .andExpect(result -> assertEquals("У этого пользователя нет доступных вещей",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
    }

    private void createItem(ItemDto itemDto) throws Exception {
        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", (long) 1)
                        .content(mapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk());
    }

    private void createUser(UserDto userDto) throws Exception {
        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(userDto)))
                .andExpect(status().isOk());
    }

    private void postBooking(BookingStartDto BookingStartDto, long userId) throws Exception {
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(BookingStartDto)))
                .andExpect(status().isOk());
    }
}
