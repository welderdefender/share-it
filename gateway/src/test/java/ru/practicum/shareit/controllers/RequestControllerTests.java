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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.MethodArgumentNotValidException;
import ru.practicum.shareit.errors.ErrorHandler;
import ru.practicum.shareit.request.RequestClient;
import ru.practicum.shareit.request.RequestController;
import ru.practicum.shareit.request.dto.RequestDto;

import javax.validation.ConstraintViolationException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(classes = RequestController.class)
@AutoConfigureMockMvc
@WebMvcTest(RequestController.class)
class RequestControllerTests {
    @Autowired
    private RequestController requestController;
    @MockBean
    private RequestClient requestClient;
    private MockMvc mockMvc;
    private static RequestDto requestDto;
    private static ResponseEntity<Object> responseIsOk;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    public static void beforeAll() {
        requestDto = RequestDto.builder()
                .description("Ищу горный велосипед")
                .build();

        responseIsOk = ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }

    @BeforeEach
    void beforeEach() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(requestController)
                .setControllerAdvice(new ErrorHandler())
                .build();
        mapper.registerModule(new JavaTimeModule());
    }

    @Test
    void ifCreateCorrectRequestThenStatusIsOk() throws Exception {
        Mockito
                .when(requestClient.create(2L, requestDto))
                .thenReturn(responseIsOk);

        mockMvc.perform(post("/requests")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 2L)
                        .content(mapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());

        Mockito.verify(requestClient, Mockito.times(1))
                .create(2L, requestDto);
    }

    @Test
    void ifCreateRequestWithoutDescriptionThenStatusIsBadRequest() throws Exception {
        RequestDto noDescription = new RequestDto();

        mockMvc.perform(post("/requests")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 2L)
                        .content(mapper.writeValueAsString(noDescription)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(status().isBadRequest());

        Mockito.verify(requestClient, Mockito.never())
                .create(Mockito.anyLong(), Mockito.any(RequestDto.class));
    }

    @Test
    void ifCreateRequestWithBlankDescriptionThenStatusIsBadRequest() throws Exception {
        RequestDto noDescription = RequestDto.builder()
                .description("")
                .build();

        mockMvc.perform(post("/requests")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 2L)
                        .content(mapper.writeValueAsString(noDescription)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(status().isBadRequest());

        Mockito.verify(requestClient, Mockito.never())
                .create(Mockito.anyLong(), Mockito.any(RequestDto.class));
    }

    @Test
    void ifGettingAllByUserIdThenStatusIsOk() throws Exception {
        Mockito
                .when(requestClient.findAllByUserId(2L))
                .thenReturn(responseIsOk);

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 2L))
                .andExpect(status().isOk());

        Mockito.verify(requestClient, Mockito.times(1))
                .findAllByUserId(2L);
    }

    @Test
    void ifGettingValidRequestByIdThenStatusIsOk() throws Exception {
        Mockito
                .when(requestClient.findByRequestId(2L, 3L))
                .thenReturn(responseIsOk);

        mockMvc.perform(get("/requests/3")
                        .header("X-Sharer-User-Id", 2L))
                .andExpect(status().isOk());

        Mockito.verify(requestClient, Mockito.times(1))
                .findByRequestId(2L, 3L);
    }

    @Test
    void ifGettingRequestWithNegativeIdThenStatusIsBadRequest() throws Exception {
        mockMvc.perform(get("/requests/-3")
                        .header("X-Sharer-User-Id", 2L))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConstraintViolationException))
                .andExpect(status().isBadRequest());

        Mockito.verify(requestClient, Mockito.never())
                .findByRequestId(Mockito.anyLong(), Mockito.anyLong());
    }

    @Test
    void ifGettingAllWithSizeIsLessThanOneThenStatusIsBadRequest() throws Exception {
        mockMvc.perform(get("/requests/all?size=-1")
                        .header("X-Sharer-User-Id", 2L))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConstraintViolationException))
                .andExpect(status().isBadRequest());

        Mockito.verify(requestClient, Mockito.never())
                .findAll(Mockito.anyLong(), Mockito.anyInt(), Mockito.anyInt());
    }

    @Test
    void ifGettingAllThenStatusIsOk() throws Exception {
        Mockito
                .when(requestClient.findAll(2L, 0, 10))
                .thenReturn(responseIsOk);

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 2L))
                .andExpect(status().isOk());

        Mockito.verify(requestClient, Mockito.times(1))
                .findAll(2L, 0, 10);
    }

    @Test
    void ifGettingAllWithNegativeFromThenStatusIsBadRequest() throws Exception {
        mockMvc.perform(get("/requests/all?from=-5&size=1")
                        .header("X-Sharer-User-Id", 2L))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConstraintViolationException))
                .andExpect(status().isBadRequest());

        Mockito.verify(requestClient, Mockito.never())
                .findAll(Mockito.anyLong(), Mockito.anyInt(), Mockito.anyInt());
    }
}
