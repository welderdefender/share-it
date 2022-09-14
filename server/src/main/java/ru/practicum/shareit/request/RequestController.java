package ru.practicum.shareit.request;

import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.RequestDtoItems;
import ru.practicum.shareit.request.dto.RequestShortDto;
import ru.practicum.shareit.request.service.RequestService;

import java.util.List;

@RestController
@RequestMapping(path = "/requests")
public class RequestController {
    private final RequestService requestService;

    public RequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping
    public ItemRequestDto create(@RequestHeader(value = "X-Sharer-User-Id") long userId,
                                 @RequestBody RequestShortDto requestInDto) {
        return requestService.create(userId, requestInDto);
    }

    @GetMapping("/{requestId}")
    public RequestDtoItems findById(@RequestHeader("X-Sharer-User-Id") long userId,
                                    @PathVariable long requestId) {
        return requestService.findById(userId, requestId);
    }

    @GetMapping("/all")
    public List<RequestDtoItems> findAllUsersRequests(@RequestHeader("X-Sharer-User-Id") long userId,
                                                      @RequestParam(value = "from") int from,
                                                      @RequestParam(value = "size") int size) {
        return requestService.findAllUsersRequests(userId, from, size);
    }

    @GetMapping
    public List<RequestDtoItems> findByUserId(@RequestHeader("X-Sharer-User-Id") long userId) {
        return requestService.findByUserId(userId);
    }
}
