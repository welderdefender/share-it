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
import ru.practicum.shareit.errors.ErrorHandler;
import ru.practicum.shareit.errors.exceptions.IllegalPaginationException;
import ru.practicum.shareit.errors.exceptions.RequestNotFoundException;
import ru.practicum.shareit.errors.exceptions.UserNotFoundException;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.requests.RequestController;
import ru.practicum.shareit.requests.dto.RequestShortDto;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.dto.UserDto;

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
class RequestControllerTests {
    @Autowired
    private RequestController requestController;
    @Autowired
    private ItemController itemController;
    @Autowired
    private UserController userController;
    private static UserDto userDto;
    private static RequestShortDto requestShortDto;
    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    public static void beforeAll() {
        userDto = UserDto.builder()
                .email("test@ya.ru")
                .name("Тестировщик")
                .build();

        requestShortDto = RequestShortDto.builder()
                .description("Ищу хороший велосипед")
                .build();
    }

    @BeforeEach
    void beforeEach() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(itemController, userController, requestController)
                .setControllerAdvice(new ErrorHandler())
                .build();
        mapper.registerModule(new JavaTimeModule());
    }

    @Test
    void createValidRequestStatusIsOk() throws Exception {
        createUser(userDto);
        mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(requestShortDto)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("userId").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("description").value("Ищу хороший велосипед"))
                .andExpect(MockMvcResultMatchers.jsonPath("created").exists());
    }

    @Test
    void createRequestWithBlankDescriptionAndStatusIsBadRequest() throws Exception {
        RequestShortDto blank = RequestShortDto.builder()
                .description(" ")
                .build();

        mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(blank)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createRequestByUserDoesNotExistsStatusIsNotFound() throws Exception {
        mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(requestShortDto)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof UserNotFoundException))
                .andExpect(result -> assertEquals("Пользователя с таким id не существует",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
    }

    @Test
    void getAllOtherUsersRequestsStatusIsOk() throws Exception {
        createUser(userDto);
        UserDto user = UserDto.builder()
                .name("Тифомей")
                .email("tima@ya.ru")
                .build();
        createUser(user);

        RequestShortDto requestOne = RequestShortDto.builder()
                .description("Тест1")
                .build();
        createRequest(requestOne, 1L);
        RequestShortDto requestTwo = RequestShortDto.builder()
                .description("Тест2")
                .build();
        createRequest(requestTwo, 1L);
        RequestShortDto requestThree = RequestShortDto.builder()
                .description("Тифомей")
                .build();
        createRequest(requestThree, 2L);

        mockMvc.perform(get("/requests/all?from=0&size=5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 2L)
                        .content(mapper.writeValueAsString(requestShortDto)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value("2"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].description").value("Тест2"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].description").value("Тест1"));
    }

    @Test
    void getAllByUserAndStatusIsOk() throws Exception {
        createUser(userDto);
        UserDto user = UserDto.builder()
                .name("Тифомей")
                .email("tima@ya.ru")
                .build();
        createUser(user);

        RequestShortDto requestOne = RequestShortDto.builder()
                .description("Тест1")
                .build();
        createRequest(requestOne, 1L);
        RequestShortDto requestTwo = RequestShortDto.builder()
                .description("Тест2")
                .build();
        createRequest(requestTwo, 1L);
        RequestShortDto requestThree = RequestShortDto.builder()
                .description("Тест3")
                .build();
        createRequest(requestThree, 2L);

        ItemDto itemToRequestTwo = ItemDto.builder()
                .requestId(2L)
                .available(true)
                .name("Велосипед мужской")
                .description("21 скорость")
                .build();
        createItem(itemToRequestTwo);

        mockMvc.perform(get("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(requestShortDto)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value("2"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].description").value("Тест2"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].items.[0].id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].items.[0].name").value("Велосипед мужской"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].items.[0].description").value("21 скорость"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].items.[0].available").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].description").value("Тест1"));
    }

    @Test
    void getAllByUserIdByAndUserNotExistsAndStatusIsNotFound() throws Exception {
        createUser(userDto);
        RequestShortDto requestOne = RequestShortDto.builder()
                .description("Тест1")
                .build();
        createRequest(requestOne, 1L);
        RequestShortDto requestTwo = RequestShortDto.builder()
                .description("Тест2")
                .build();
        createRequest(requestTwo, 1L);

        mockMvc.perform(get("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 3L)
                        .content(mapper.writeValueAsString(requestShortDto)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof UserNotFoundException))
                .andExpect(result -> assertEquals("Пользователя с таким id не существует",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
    }

    @Test
    void getAllIncorrectPageArgumentsStatusIsBadRequest() throws Exception {
        createUser(userDto);
        UserDto user = UserDto.builder()
                .name("Тифомей")
                .email("tima@ya.ru")
                .build();
        createUser(user);

        RequestShortDto requestOne = RequestShortDto.builder()
                .description("Тест1")
                .build();
        createRequest(requestOne, 1L);
        RequestShortDto requestTwo = RequestShortDto.builder()
                .description("Тест2")
                .build();
        createRequest(requestTwo, 1L);
        RequestShortDto requestThree = RequestShortDto.builder()
                .description("Тифомей")
                .build();
        createRequest(requestThree, 2L);

        mockMvc.perform(get("/requests/all?from=0&size=-5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 2L)
                        .content(mapper.writeValueAsString(requestShortDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof IllegalPaginationException))
                .andExpect(result -> assertEquals("переменная size должна быть больше, либо равна 1",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
    }

    @Test
    void getByRequestIdDoesNotExistsAndStatusIsNotFound() throws Exception {
        createUser(userDto);

        mockMvc.perform(get("/requests/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(requestShortDto)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof RequestNotFoundException))
                .andExpect(result -> assertEquals("Запроса с таким id не найдено",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
    }

    @Test
    void getByRequestIdAndStatusIsOk() throws Exception {
        createUser(userDto);
        createRequest(requestShortDto, 1L);

        mockMvc.perform(get("/requests/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(requestShortDto)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("description").value("Ищу хороший велосипед"));
    }

    @Test
    void getByRequestIdAndUserDoesNotExistsAndStatusIsNotFound() throws Exception {
        mockMvc.perform(get("/requests/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(requestShortDto)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof UserNotFoundException))
                .andExpect(result -> assertEquals("Пользователя с таким id не существует",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
    }

    private void createItem(ItemDto itemDto) throws Exception {
        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", (long) 2)
                        .content(mapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk());
    }

    private void createUser(UserDto userDto) throws Exception {
        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(userDto)))
                .andExpect(status().isOk());
    }

    private void createRequest(RequestShortDto requestShortDto, long userId) throws Exception {
        mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(requestShortDto)))
                .andExpect(status().isOk());
    }
}
