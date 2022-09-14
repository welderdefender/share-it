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
import ru.practicum.shareit.errors.ErrorHandler;
import ru.practicum.shareit.item.ItemClient;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.ConstraintViolationException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(classes = ItemController.class)
@AutoConfigureMockMvc
@WebMvcTest(ItemController.class)
class ItemControllerTests {
    @Autowired
    private ItemController itemController;
    @MockBean
    private ItemClient itemClient;
    private MockMvc mockMvc;
    private static ItemDto itemDto;
    private static CommentDto commentDto;
    private static ResponseEntity<Object> responseIsOk;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    public static void beforeAll() {
        itemDto = ItemDto.builder()
                .available(true)
                .name("Велосипед горный")
                .description("для эмоциональных поездок")
                .build();

        commentDto = CommentDto.builder()
                .authorName("Павел")
                .text("Неплохой велосипед, но отвалилось колесо")
                .build();

        responseIsOk = ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }

    @BeforeEach
    void beforeEach() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(itemController)
                .setControllerAdvice(new ErrorHandler())
                .build();
        mapper.registerModule(new JavaTimeModule());
    }

    @Test
    void ifCreatingCorrectItemThenStatusIsOk() throws Exception {
        Mockito
                .when(itemClient.create(2L, itemDto))
                .thenReturn(responseIsOk);

        mockMvc.perform(post("/items")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 2L)
                        .content(mapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk());

        Mockito.verify(itemClient, Mockito.times(1))
                .create(2L, itemDto);
    }

    @Test
    void ifCreatingItemWithNameIsNullThenStatusIsBadRequest() throws Exception {
        ItemDto withoutName = ItemDto.builder()
                .available(true)
                .description("для эмоциональных поездок")
                .build();

        mockMvc.perform(post("/items")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 2L)
                        .content(mapper.writeValueAsString(withoutName)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(status().isBadRequest());

        Mockito.verify(itemClient, Mockito.never())
                .create(2L, withoutName);
    }

    @Test
    void ifCreatingItemWithBlankDescriptionThenStatusIsBadRequest() throws Exception {
        ItemDto withBlankDescription = ItemDto.builder()
                .name("Еще какой-то велик")
                .available(true)
                .description("\n")
                .build();

        mockMvc.perform(post("/items")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 2L)
                        .content(mapper.writeValueAsString(withBlankDescription)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(status().isBadRequest());

        Mockito.verify(itemClient, Mockito.never())
                .create(2L, withBlankDescription);
    }

    @Test
    void ifCreatingItemWithBlankNameThenStatusIsBadRequest() throws Exception {
        ItemDto withBlankName = ItemDto.builder()
                .name("")
                .available(true)
                .description("для эмоциональных поездок")
                .build();

        mockMvc.perform(post("/items")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 2L)
                        .content(mapper.writeValueAsString(withBlankName)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(status().isBadRequest());

        Mockito.verify(itemClient, Mockito.never())
                .create(2L, withBlankName);
    }

    @Test
    void ifCreatingItemWithNullDescriptionThenStatusIsBadRequest() throws Exception {
        ItemDto withoutDescription = ItemDto.builder()
                .name("Какой-то велик")
                .available(true)
                .build();

        mockMvc.perform(post("/items")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 2L)
                        .content(mapper.writeValueAsString(withoutDescription)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(status().isBadRequest());

        Mockito.verify(itemClient, Mockito.never())
                .create(2L, withoutDescription);
    }

    @Test
    void ifCommentingWithTextIsNullThenStatusIsBadRequest() throws Exception {
        CommentDto commentWithoutText = CommentDto.builder()
                .authorName("Павел")
                .build();

        mockMvc.perform(post("/items/3/comment")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(commentWithoutText)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(status().isBadRequest());

        Mockito.verify(itemClient, Mockito.never())
                .createComment(1L, commentWithoutText, 3L);
    }

    @Test
    void ifCreatingItemAndAvailabilityIsNullThenStatusIsBadRequest() throws Exception {
        ItemDto withoutAvailable = ItemDto.builder()
                .name("Недоступный велик")
                .description("На нем нельзя кататься")
                .build();

        mockMvc.perform(post("/items")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 2L)
                        .content(mapper.writeValueAsString(withoutAvailable)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(status().isBadRequest());

        Mockito.verify(itemClient, Mockito.never())
                .create(2L, withoutAvailable);
    }

    @Test
    void ifPostingCorrectCommentThenStatusIsOk() throws Exception {
        Mockito
                .when(itemClient.createComment(1L, commentDto, 3L))
                .thenReturn(responseIsOk);

        mockMvc.perform(post("/items/3/comment")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk());

        Mockito.verify(itemClient, Mockito.times(1))
                .createComment(1L, commentDto, 3L);
    }

    @Test
    void ifUpdatingCorrectItemThenStatusIsOk() throws Exception {
        Mockito
                .when(itemClient.update(1L, itemDto, 2L))
                .thenReturn(responseIsOk);

        mockMvc.perform(patch("/items/2")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk());

        Mockito.verify(itemClient, Mockito.times(1))
                .update(1L, itemDto, 2L);
    }

    @Test
    void ifPostingCommentWithBlankTextThenStatusIsBadRequest() throws Exception {
        CommentDto commentWithBlankText = CommentDto.builder()
                .authorName("Павел")
                .text("\t")
                .build();

        mockMvc.perform(post("/items/3/comment")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(commentWithBlankText)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(status().isBadRequest());

        Mockito.verify(itemClient, Mockito.never())
                .createComment(1L, commentWithBlankText, 3L);
    }

    @Test
    void ifPostingCommentWithNegativeItemIdThenStatusIsBadRequest() throws Exception {
        mockMvc.perform(post("/items/-3/comment")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(commentDto)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConstraintViolationException))
                .andExpect(status().isBadRequest());

        Mockito.verify(itemClient, Mockito.never())
                .createComment(1L, commentDto, -3L);
    }

    @Test
    void ifGettingItemWithNegativeIdThenStatusIsBadRequest() throws Exception {
        mockMvc.perform(get("/items/-2")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConstraintViolationException))
                .andExpect(status().isBadRequest());

        Mockito.verify(itemClient, Mockito.never())
                .findByItemId(1L, -2L);
    }

    @Test
    void ifUpdatingItemWithNegativeItemIdThenStatusIsBadRequest() throws Exception {
        mockMvc.perform(patch("/items/-2")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(itemDto)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConstraintViolationException))
                .andExpect(status().isBadRequest());

        Mockito.verify(itemClient, Mockito.never())
                .update(1L, itemDto, -2L);
    }

    @Test
    void ifGettingCorrectItemByIdThenStatusIsOk() throws Exception {
        Mockito
                .when(itemClient.findByItemId(1L, 2L))
                .thenReturn(responseIsOk);

        mockMvc.perform(get("/items/2")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());

        Mockito.verify(itemClient, Mockito.times(1))
                .findByItemId(1L, 2L);
    }

    @Test
    void ifGettingItemByUserIdWithSizeLessThanOneThenStatusIsBadRequest() throws Exception {
        mockMvc.perform(get("/items?from=3&size=0")
                        .header("X-Sharer-User-Id", 5L))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConstraintViolationException))
                .andExpect(status().isBadRequest());

        Mockito.verify(itemClient, Mockito.never())
                .findByUserId(5L, -3, 0);
    }

    @Test
    void ifGettingCorrectItemByUserIdThenStatusIsOk() throws Exception {
        Mockito
                .when(itemClient.findByUserId(5L, 3, 2))
                .thenReturn(responseIsOk);

        mockMvc.perform(get("/items?from=3&size=2")
                        .header("X-Sharer-User-Id", 5L))
                .andExpect(status().isOk());

        Mockito.verify(itemClient, Mockito.times(1))
                .findByUserId(5L, 3, 2);
    }

    @Test
    void ifGettingItemByUserIdWithNegativeFromThenStatusIsBadRequest() throws Exception {
        mockMvc.perform(get("/items?from=-3&size=2")
                        .header("X-Sharer-User-Id", 5L))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConstraintViolationException))
                .andExpect(status().isBadRequest());

        Mockito.verify(itemClient, Mockito.never())
                .findByUserId(5L, -3, 2);
    }

    @Test
    void ifSearchingWithSizeLessThanOneThenStatusIsBadRequest() throws Exception {
        mockMvc.perform(get("/items/search?text=bike&from=3&size=0"))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConstraintViolationException))
                .andExpect(status().isBadRequest());

        Mockito.verify(itemClient, Mockito.never())
                .searchText("bike", 3, 0);
    }

    @Test
    void ifSearchingThenStatusIsOk() throws Exception {
        Mockito
                .when(itemClient.searchText("bike", 0, 10))
                .thenReturn(responseIsOk);

        mockMvc.perform(get("/items/search?text=bike"))
                .andExpect(status().isOk());

        Mockito.verify(itemClient, Mockito.times(1))
                .searchText("bike", 0, 10);
    }

    @Test
    void ifSearchingWithNegativeFromThenStatusIsBadRequest() throws Exception {
        mockMvc.perform(get("/items/search?text=bike&from=-3&size=3"))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConstraintViolationException))
                .andExpect(status().isBadRequest());

        Mockito.verify(itemClient, Mockito.never())
                .searchText("bike", -3, 3);
    }
}
