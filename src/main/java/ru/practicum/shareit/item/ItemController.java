package ru.practicum.shareit.item;

import java.util.List;
import javax.validation.Valid;

import ru.practicum.shareit.item.service.ItemService;

import ru.practicum.shareit.item.dto.ItemDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    public ItemDto create(@RequestHeader(value = "X-Sharer-User-Id") long id, @Valid @RequestBody ItemDto item) {
        return itemService.create(id, item);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader("X-Sharer-User-Id") long id, @RequestBody ItemDto item,
                          @PathVariable long itemId) {
        return itemService.update(id, itemId, item);
    }

    @GetMapping("/{itemId}")
    public ItemDto findById(@PathVariable long itemId) {
        return itemService.findById(itemId);
    }

    @GetMapping
    public List<ItemDto> findByOwner(@RequestHeader("X-Sharer-User-Id") long userId) {
        return itemService.findByOwner(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> findByText(@RequestParam(value = "text") String text) {
        return itemService.findByText(text);
    }
}
