package ru.practicum.shareit.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.shareit.errors.ErrorHandler;
import ru.practicum.shareit.errors.exceptions.DuplicateEmailException;
import ru.practicum.shareit.errors.exceptions.UserNotFoundException;
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
class UserControllerTests {
    @Autowired
    private UserController userController;
    private final ObjectMapper mapper = new ObjectMapper();
    private static UserDto userDto;
    private MockMvc mockMvc;

    @BeforeAll
    public static void beforeAll() {
        userDto = UserDto.builder()
                .name("Тестировщик")
                .email("test@ya.ru")
                .build();
    }

    @BeforeEach
    void beforeEach() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .setControllerAdvice(new ErrorHandler())
                .build();
    }

    @Test
    void createUserStatusIsOk() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("name").value("Тестировщик"))
                .andExpect(MockMvcResultMatchers.jsonPath("email").value("test@ya.ru"));
    }

    @Test
    void updateUserStatusIsOk() throws Exception {
        createUser(userDto);
        UserDto userToUpdate = UserDto.builder()
                .name("Ирина")
                .email("irina@ya.ru")
                .build();

        mockMvc.perform(patch("/users/1")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(userToUpdate)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("name").value("Ирина"))
                .andExpect(MockMvcResultMatchers.jsonPath("email").value("irina@ya.ru"));
    }

    @Test
    void updateUserDoesNotExistsStatusIsNotFound() throws Exception {
        createUser(userDto);
        UserDto userToUpdate = UserDto.builder()
                .name("Ирина")
                .email("irina@ya.ru")
                .build();

        mockMvc.perform(patch("/users/10")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(userToUpdate)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof UserNotFoundException))
                .andExpect(result -> assertEquals("Пользователь с таким id не найден",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateUserDuplicateEmailStatusIsBad() throws Exception {
        UserDto duplicateEmailUser = UserDto.builder()
                .name("Ирочка")
                .email("irina@ya.ru")
                .build();

        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(duplicateEmailUser)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("email").value("irina@ya.ru"));

        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("email").value("test@ya.ru"));

        UserDto userToUpdate = UserDto.builder()
                .name("Ирина")
                .email("irina@ya.ru")
                .build();

        mockMvc.perform(patch("/users/2")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(userToUpdate)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof DuplicateEmailException))
                .andExpect(result -> assertEquals("Пользователь с таким Email уже зарегистрирован",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()))
                .andExpect(status().isConflict());
    }

    @Test
    void findUserByIdStatusIsOk() throws Exception {
        createUser(userDto);
        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("name").value("Тестировщик"))
                .andExpect(MockMvcResultMatchers.jsonPath("email").value("test@ya.ru"));
    }

    @Test
    void findUserDoesNotExistsByIdStatusIsNotFound() throws Exception {
        mockMvc.perform(get("/users/1"))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof UserNotFoundException))
                .andExpect(result -> assertEquals("Пользователь с таким id не найден",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()))
                .andExpect(status().isNotFound());
    }

    @Test
    void findAll() throws Exception {
        createUser(userDto);
        UserDto user = UserDto.builder()
                .name("Павел")
                .email("pavel@ya.ru")
                .build();
        createUser(user);

        mockMvc.perform(get("/users/"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value("Тестировщик"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].email").value("test@ya.ru"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].id").value("2"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].name").value("Павел"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].email").value("pavel@ya.ru"));
    }

    @Test
    void deleteUserByIdStatusIsOk() throws Exception {
        createUser(userDto);
        mockMvc.perform(delete("/users/1")
                        .contentType("application/json"))
                .andExpect(status().isOk());
    }

    private void createUser(UserDto userDto) throws Exception {
        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(userDto)))
                .andExpect(status().isOk());
    }
}
