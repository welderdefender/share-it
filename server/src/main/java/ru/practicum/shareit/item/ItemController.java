package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBookings;
import ru.practicum.shareit.item.dto.ItemDtoWithComments;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    public ItemDto create(@RequestHeader(value = "X-Sharer-User-Id") long id, @RequestBody ItemDto item) {
        return itemService.create(id, item);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader("X-Sharer-User-Id") long id, @RequestBody ItemDto item,
                          @PathVariable long itemId) {
        return itemService.update(id, itemId, item);
    }

    @GetMapping("/{itemId}")
    public ItemDtoWithComments findById(@RequestHeader("X-Sharer-User-Id") long userId, @PathVariable long itemId) {
        return itemService.findById(userId, itemId);
    }

    @GetMapping
    public List<ItemDtoWithBookings> findByOwner(@RequestHeader("X-Sharer-User-Id") long userId,
                                                 @RequestParam(value = "from") int from,
                                                 @RequestParam(value = "size") int size) {
        return itemService.findByOwner(userId, from, size);
    }

    @GetMapping("/search")
    public List<ItemDto> searchText(@RequestParam(value = "text") String text,
                                    @RequestParam(value = "from") int from,
                                    @RequestParam(value = "size") int size) {
        return itemService.searchText(text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto makeComment(@RequestHeader("X-Sharer-User-Id") long userId, @RequestBody CommentDto commentDto,
                                  @PathVariable long itemId) {
        return itemService.createComment(commentDto, userId, itemId);
    }
}
