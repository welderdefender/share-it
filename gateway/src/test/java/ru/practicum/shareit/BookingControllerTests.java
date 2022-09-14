package ru.practicum.shareit;

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

import ru.practicum.shareit.booking.BookingClient;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.errors.BadRequestException;
import ru.practicum.shareit.errors.ErrorHandler;

import javax.validation.ConstraintViolationException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(classes = BookingController.class)
@WebMvcTest(BookingController.class)
@AutoConfigureMockMvc
class BookingControllerTests {
    @Autowired
    private BookingController bookingController;
    @MockBean
    private BookingClient bookingClient;
    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();
    private static BookingDto bookingDto;
    private static ResponseEntity<Object> responseIsOk;

    @BeforeAll
    public static void beforeAll() {
        bookingDto = BookingDto.builder()
                .bookerId(2L)
                .itemId(3L)
                .start(LocalDateTime.now().plusHours(2))
                .end(LocalDateTime.now().plusDays(1))
                .build();

        responseIsOk = ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }

    @BeforeEach
    void beforeEach() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(bookingController)
                .setControllerAdvice(new ErrorHandler())
                .build();
        mapper.registerModule(new JavaTimeModule());
    }

    @Test
    void ifCreatingCorrectBookingThenStatusIsOk() throws Exception {
        Mockito
                .when(bookingClient.create(2L, bookingDto))
                .thenReturn(responseIsOk);
        mockMvc.perform(post("/bookings")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 2L)
                        .content(mapper.writeValueAsString(bookingDto)))
                .andExpect(status().isOk());

        Mockito.verify(bookingClient, Mockito.times(1))
                .create(2L, bookingDto);
    }

    @Test
    void ifBookingWithNegativeItemIdThenStatusIsBadRequest() throws Exception {
        BookingDto itemId = BookingDto.builder()
                .bookerId(2L)
                .itemId(-3L)
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusDays(1))
                .build();

        mockMvc.perform(post("/bookings")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 2L)
                        .content(mapper.writeValueAsString(itemId)))
                .andExpect(status().isBadRequest());

        Mockito.verify(bookingClient, Mockito.never())
                .create(Mockito.anyLong(), Mockito.any(BookingDto.class));
    }

    @Test
    void ifBookingWithStartBeforeNowThenStatusIsBadRequest() throws Exception {
        BookingDto startBefore = BookingDto.builder()
                .bookerId(2L)
                .itemId(3L)
                .start(LocalDateTime.now().minusDays(1))
                .end(LocalDateTime.now().plusDays(1))
                .build();

        mockMvc.perform(post("/bookings")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 2L)
                        .content(mapper.writeValueAsString(startBefore)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(status().isBadRequest());

        Mockito.verify(bookingClient, Mockito.never())
                .create(Mockito.anyLong(), Mockito.any(BookingDto.class));
    }

    @Test
    void ifBookingWithNegativeBookerIdThenStatusIsBadRequest() throws Exception {
        BookingDto bookerId = BookingDto.builder()
                .bookerId(-2L)
                .itemId(3L)
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusDays(1))
                .build();

        mockMvc.perform(post("/bookings")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 2L)
                        .content(mapper.writeValueAsString(bookerId)))
                .andExpect(status().isBadRequest());

        Mockito.verify(bookingClient, Mockito.never())
                .create(Mockito.anyLong(), Mockito.any(BookingDto.class));
    }

    @Test
    void ifUpdatingCorrectStatusThenStatusIsOk() throws Exception {
        Mockito
                .when(bookingClient.update(1L, 1L, true))
                .thenReturn(responseIsOk);

        mockMvc.perform(patch("/bookings/1?approved=true")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());

        Mockito.verify(bookingClient, Mockito.times(1))
                .update(1L, 1L, true);
    }

    @Test
    void ifGettingCorrectByIdThenStatusIsOk() throws Exception {
        Mockito
                .when(bookingClient.findById(1L, 1L))
                .thenReturn(responseIsOk);

        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());

        Mockito.verify(bookingClient, Mockito.times(1))
                .findById(1L, 1L);
    }

    @Test
    void ifGettingWithNegativeIdThenStatusIsBadRequest() throws Exception {
        mockMvc.perform(get("/bookings/-1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isBadRequest());

        Mockito.verify(bookingClient, Mockito.never())
                .findById(1L, -1L);
    }

    @Test
    void ifBookingWithStartBeforeEndThenStatusIsBadRequest() throws Exception {
        Mockito
                .when(bookingClient.create(2L, bookingDto))
                .thenThrow(new BadRequestException("Начало бронирования должно быть раньше окончания"));

        mockMvc.perform(post("/bookings")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 2L)
                        .content(mapper.writeValueAsString(bookingDto)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof BadRequestException))
                .andExpect(result -> assertEquals("Начало бронирования должно быть раньше окончания",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()))
                .andExpect(status().isBadRequest());

        Mockito.verify(bookingClient, Mockito.times(1))
                .create(2L, bookingDto);
    }

    @Test
    void ifGettingUserBookingsAndInvalidFromThenBadRequest() throws Exception {
        mockMvc.perform(get("/bookings?state=ALL&from=-12")
                        .header("X-Sharer-User-Id", 2L))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConstraintViolationException))
                .andExpect(result -> assertEquals("getUserBookings.from: must be greater than or equal to 0",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ifGettingUserBookingsAndInvalidSizeThenBadRequest() throws Exception {
        mockMvc.perform(get("/bookings?state=ALL&from=12&size=-1")
                        .header("X-Sharer-User-Id", 2L))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConstraintViolationException))
                .andExpect(result -> assertEquals("getUserBookings.size: must be greater than 0",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ifGettingBookingsByOwnerIdAndInvalidFromThenBadRequest() throws Exception {
        mockMvc.perform(get("/bookings/owner?state=ALL&from=-12")
                        .header("X-Sharer-User-Id", 2L))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConstraintViolationException))
                .andExpect(result -> assertEquals("getBookingsByOwner.from: must be greater than or equal to 0",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ifGettingBookingsByOwnerIdAndSizeIsZeroThenBadRequest() throws Exception {
        mockMvc.perform(get("/bookings/owner?state=ALL&from=12&size=0")
                        .header("X-Sharer-User-Id", 2L))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConstraintViolationException))
                .andExpect(result -> assertEquals("getBookingsByOwner.size: must be greater than 0",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ifGettingBookingsByOwnerIdAndInvalidSizeThenBadRequest() throws Exception {
        mockMvc.perform(get("/bookings/owner?state=ALL&from=12&size=-1")
                        .header("X-Sharer-User-Id", 2L))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConstraintViolationException))
                .andExpect(result -> assertEquals("getBookingsByOwner.size: must be greater than 0",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()))
                .andExpect(status().isBadRequest());
    }
}
