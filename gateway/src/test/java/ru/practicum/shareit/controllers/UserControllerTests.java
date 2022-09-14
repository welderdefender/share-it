package ru.practicum.shareit.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.MethodArgumentNotValidException;
import ru.practicum.shareit.errors.ErrorHandler;
import ru.practicum.shareit.user.UserClient;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.ConstraintViolationException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(classes = UserController.class)
@AutoConfigureMockMvc
@WebMvcTest(UserController.class)
class UserControllerTests {
    @Autowired
    private UserController userController;
    @MockBean
    private UserClient userClient;
    private MockMvc mockMvc;
    private static UserDto userDto;
    private static ResponseEntity<Object> responseIsOk;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    public static void beforeAll() {
        userDto = UserDto.builder()
                .email("serafim@yandex.ru")
                .name("Серафим")
                .build();
    }

    @BeforeEach
    void beforeEach() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .setControllerAdvice(new ErrorHandler())
                .build();
        mapper.registerModule(new JavaTimeModule());
    }

    @Test
    void ifCreatingCorrectUserThenStatusIsOk() throws Exception {
        Mockito
                .when(userClient.create(userDto))
                .thenReturn(responseIsOk);

        mockMvc.perform(post("/users")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(userDto)))
                .andExpect(status().isOk());

        Mockito.verify(userClient, Mockito.times(1))
                .create(userDto);
    }

    @Test
    void ifCreatingUserWithoutEmailThenStatusIsBadRequest() throws Exception {
        UserDto withoutEmail = UserDto.builder()
                .name("яБезПочты")
                .build();

        mockMvc.perform(post("/users")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(withoutEmail)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(status().isBadRequest());

        Mockito.verify(userClient, Mockito.never())
                .create(Mockito.any(UserDto.class));
    }

    @Test
    void ifUpdatingCorrectUserThenStatusIsOk() throws Exception {
        Mockito
                .when(userClient.update(1L, userDto))
                .thenReturn(responseIsOk);

        mockMvc.perform(patch("/users/1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(userDto)))
                .andExpect(status().isOk());

        Mockito.verify(userClient, Mockito.times(1))
                .update(1L, userDto);
    }

    @Test
    void ifCreatingUserWithBlankEmailThenStatusIsBadRequest() throws Exception {
        UserDto withBlankEmail = UserDto.builder()
                .name("уМеняПустаяПочта")
                .email("\n")
                .build();

        mockMvc.perform(post("/users")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(withBlankEmail)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(status().isBadRequest());

        Mockito.verify(userClient, Mockito.never())
                .create(Mockito.any(UserDto.class));
    }

    @Test
    void ifCreatingUserWithInvalidEmailThenStatusIsBadRequest() throws Exception {
        UserDto withIncorrectEmail = UserDto.builder()
                .name("мояПочтаНеВерна")
                .email("qwerty&yandex.ru")
                .build();

        mockMvc.perform(post("/users")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(withIncorrectEmail)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(status().isBadRequest());

        Mockito.verify(userClient, Mockito.never())
                .create(Mockito.any(UserDto.class));
    }

    @Test
    void ifGettingUserWithNegativeIdThenStatusIsBadRequest() throws Exception {
        mockMvc.perform(get("/users/-4")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConstraintViolationException))
                .andExpect(status().isBadRequest());

        Mockito.verify(userClient, Mockito.never())
                .findById(Mockito.anyLong());
    }

    @Test
    void ifUpdatingUserWithNegativeUserIdThenStatusIsBadRequest() throws Exception {
        mockMvc.perform(patch("/users/-5")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(userDto)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConstraintViolationException))
                .andExpect(status().isBadRequest());

        Mockito.verify(userClient, Mockito.never())
                .update(Mockito.anyLong(), Mockito.any(UserDto.class));
    }

    @Test
    void ifGettingValidUserByIdThenStatusIsOk() throws Exception {
        Mockito
                .when(userClient.findById(12L))
                .thenReturn(responseIsOk);

        mockMvc.perform(get("/users/12"))
                .andExpect(status().isOk());

        Mockito.verify(userClient, Mockito.times(1))
                .findById(12L);
    }

    @Test
    void ifDeletingWithNegativeIdThenStatusIsBadRequest() throws Exception {
        mockMvc.perform(delete("/users/-12"))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConstraintViolationException))
                .andExpect(status().isBadRequest());

        Mockito.verify(userClient, Mockito.never())
                .deleteById(Mockito.anyLong());
    }

    @Test
    void ifGettingAllThenStatusIsOk() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk());

        Mockito.verify(userClient, Mockito.times(1))
                .findAll();
    }

    @Test
    void ifDeletingWithCorrectIdThenStatusIsOk() throws Exception {
        Mockito
                .when(userClient.deleteById(12L))
                .thenReturn(responseIsOk);

        mockMvc.perform(delete("/users/12"))
                .andExpect(status().isOk());

        Mockito.verify(userClient, Mockito.times(1))
                .deleteById(12L);
    }
}
