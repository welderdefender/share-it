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
import ru.practicum.shareit.errors.exceptions.IllegalPaginationException;
import ru.practicum.shareit.errors.exceptions.ItemNotFoundException;
import ru.practicum.shareit.errors.exceptions.UserNotFoundException;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
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
class ItemControllerTests {
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
    void createItemStatusIsOk() throws Exception {
        createUser(owner);
        
        mockMvc.perform(post("/items")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("name").value("велосипед"))
                .andExpect(MockMvcResultMatchers.jsonPath("description").value("горный скоростной"));
    }

    @Test
    void createItemByUserDoesNotExistsAndStatusIsNotFound() throws Exception {
        mockMvc.perform(post("/items")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 2L)
                        .content(mapper.writeValueAsString(itemDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void postCommentWhenBookingStatusIsBadRequest() throws Exception {
        createUser(owner);
        createItem(itemDto, 1L);

        UserDto booker = UserDto.builder()
                .name("арендатор")
                .email("bookertest@ya.ru")
                .build();
        createUser(booker);

        BookingStartDto bookingStartDto = BookingStartDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusSeconds(2))
                .end(LocalDateTime.now().plusHours(2))
                .build();
        postBooking(bookingStartDto);

        CommentDto commentDto = CommentDto.builder()
                .text("велосипед понравился")
                .authorName("Антон")
                .build();

        mockMvc.perform(post("/items/1/comment")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 2L)
                        .content(mapper.writeValueAsString(commentDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postValidCommentStatusIsOk() throws Exception {
        createUser(owner);
        createItem(itemDto, 1L);
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
        postBooking(bookingStartDto);

        Thread.sleep(5000L);

        CommentDto commentDto = CommentDto.builder()
                .text("велосипед понравился")
                .authorName("Антон")
                .build();

        mockMvc.perform(post("/items/1/comment")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 2L)
                        .content(mapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("text").value("велосипед понравился"))
                .andExpect(MockMvcResultMatchers.jsonPath("created").exists());
    }

    @Test
    void updateItemStatusIsOk() throws Exception {
        createUser(owner);
        createItem(itemDto, 1L);

        ItemDto itemToUpdate = ItemDto.builder()
                .available(false)
                .build();

        mockMvc.perform(patch("/items/1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(itemToUpdate)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("available").value(false));
    }

    @Test
    void getByUserId() throws Exception {
        createUser(owner);
        createItem(itemDto, 1L);

        UserDto user = UserDto.builder()
                .name("Виктор")
                .email("viktor@ya.ru")
                .build();
        createUser(user);

        ItemDto item = ItemDto.builder()
                .name("Супер велосипед")
                .description("Очень мягкое седло")
                .available(true)
                .build();
        createItem(item, 1L);
        ItemDto newItem = ItemDto.builder()
                .available(true)
                .name("Шоссейный велосипед")
                .description("быстрее ветра")
                .build();
        createItem(newItem, 2L);

        mockMvc.perform(get("/items?from=0&size=10")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value("велосипед"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].description").value("горный скоростной"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].id").value("2"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].name").value("Супер велосипед"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].description").value("Очень мягкое седло"));
    }

    @Test
    void updateItemDoesNotExistsAndStatusIsNotFound() throws Exception {
        createUser(owner);
        createItem(itemDto, 1L);

        ItemDto itemToUpdate = ItemDto.builder()
                .available(false)
                .build();

        mockMvc.perform(patch("/items/2")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(itemToUpdate)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ItemNotFoundException))
                .andExpect(result -> assertEquals("Вещь с таким id не найдена",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateItemWrongOwnerAndStatusIsNotFound() throws Exception {
        createUser(owner);
        createItem(itemDto, 1L);

        ItemDto itemToUpdate = ItemDto.builder()
                .available(false)
                .build();

        mockMvc.perform(patch("/items/1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 2L)
                        .content(mapper.writeValueAsString(itemToUpdate)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof UserNotFoundException))
                .andExpect(result -> assertEquals("Пользователь не найден",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getItemById() throws Exception {
        createUser(owner);
        createItem(itemDto, 1L);

        mockMvc.perform(get("/items/1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("name").value("велосипед"))
                .andExpect(MockMvcResultMatchers.jsonPath("description").value("горный скоростной"))
                .andExpect(MockMvcResultMatchers.jsonPath("available").value(true));
    }

    @Test
    void getByItemDoestNotExistsAndStatusNotFound() throws Exception {
        createUser(owner);
        createItem(itemDto, 1L);

        mockMvc.perform(get("/items/2")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 2L))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ItemNotFoundException))
                .andExpect(result -> assertEquals("Вещь с таким id не найдена",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByUserIdIncorrectPageArgsAndStatusIsBadRequest() throws Exception {
        createUser(owner);
        createItem(itemDto, 1L);

        mockMvc.perform(get("/items?from=-1&size=20")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof IllegalPaginationException))
                .andExpect(result -> assertEquals("Переменная from должна быть больше, либо равна 0",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
    }

    @Test
    void searchTextInvalidPageArgumentsAndStatusIsBadRequest() throws Exception {
        createUser(owner);
        createItem(itemDto, 1L);

        mockMvc.perform(get("/items/search?text=JaVa&from=0&size=-2")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof IllegalPaginationException))
                .andExpect(result -> assertEquals("переменная size должна быть больше, либо равна 1",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
    }

    private void createItem(ItemDto itemDto, long userId) throws Exception {
        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk());
    }

    private void createUser(UserDto userDto) throws Exception {
        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(userDto)))
                .andExpect(status().isOk());
    }

    private void postBooking(BookingStartDto BookingStartDto) throws Exception {
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", (long) 2)
                        .content(mapper.writeValueAsString(BookingStartDto)))
                .andExpect(status().isOk());
    }
} 
