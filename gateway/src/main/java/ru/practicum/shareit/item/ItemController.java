package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Slf4j
@Validated
@Controller
@RequestMapping(path = "/items")
@RequiredArgsConstructor

public class ItemController {
    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader(value = "X-Sharer-User-Id") long userId,
                                         @Valid @RequestBody ItemDto itemDto) {
        log.info("Вещь с id {} создана пользователем {}", itemDto, userId);
        return itemClient.create(userId, itemDto);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> createComment(@RequestHeader("X-Sharer-User-Id") long userId,
                                                @Valid @RequestBody CommentDto commentDto,
                                                @PathVariable @Positive long itemId) {
        log.info("Создан комментарий с id {}, пользователь {}, id вещи {}", commentDto, userId, itemId);
        return itemClient.createComment(userId, commentDto, itemId);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(@RequestHeader("X-Sharer-User-Id") long userId,
                                         @RequestBody ItemDto itemDto,
                                         @PathVariable @Positive long itemId) {
        log.info("Обновление вещи с id {}, пользователь {}", itemId, userId);
        return itemClient.update(userId, itemDto, itemId);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> findByItemId(@RequestHeader("X-Sharer-User-Id") long userId,
                                               @PathVariable @Positive long itemId) {
        log.info("Поиск вещи с id {}, пользователь {}", itemId, userId);
        return itemClient.findByItemId(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> findByUserId(@RequestHeader("X-Sharer-User-Id") long userId,
                                               @RequestParam(value = "from", required = false, defaultValue = "0")
                                               @PositiveOrZero int from,
                                               @RequestParam(value = "size", required = false, defaultValue = "10")
                                               @Min(1) int size) {
        log.info("Поиск всех вещей у пользователя {}, from {}, size {}", userId, from, size);
        return itemClient.findByUserId(userId, from, size);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchText(@RequestParam(value = "text") String text,
                                             @RequestParam(value = "from", required = false, defaultValue = "0")
                                             @PositiveOrZero int from,
                                             @RequestParam(value = "size", required = false, defaultValue = "10")
                                             @Min(1) int size) {
        log.info("Поиск вещи по запросу = {}, from {}, size {}",
                text, from, size);
        return itemClient.searchText(text, from, size);
    }
}
