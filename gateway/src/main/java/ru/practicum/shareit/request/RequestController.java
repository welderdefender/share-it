package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.RequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Slf4j
@Validated
@Controller
@RequestMapping(path = "/requests")
@RequiredArgsConstructor

public class RequestController {
    private final RequestClient requestClient;

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader(value = "X-Sharer-User-Id") long userId,
                                         @Valid @RequestBody RequestDto requestDto) {
        log.info("Создан запрос с id {} пользователем {}", requestDto, userId);
        return requestClient.create(userId, requestDto);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> findByRequestId(@RequestHeader("X-Sharer-User-Id") long userId,
                                                 @PathVariable @Positive long requestId) {
        log.info("Поиск запроса по id {} у пользователя {}", requestId, userId);
        return requestClient.findByRequestId(userId, requestId);
    }

    @GetMapping
    public ResponseEntity<Object> findAllByUserId(@RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("Поиск запросов у пользователя с id {}", userId);
        return requestClient.findAllByUserId(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> findAll(@RequestHeader("X-Sharer-User-Id") long userId,
                                         @RequestParam(value = "from", required = false, defaultValue = "0")
                                         @PositiveOrZero int from,
                                         @RequestParam(value = "size", required = false, defaultValue = "10")
                                         @Positive @Min(1) int size) {
        log.info("Поиск всех запросов с параметрами from {} и size {}", from, size);
        return requestClient.findAll(userId, from, size);
    }
}
