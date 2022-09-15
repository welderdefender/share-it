package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBookings;
import ru.practicum.shareit.item.dto.ItemDtoWithComments;

import java.util.List;

public interface ItemService {
    ItemDto create(long userId, ItemDto item);

    ItemDto update(long userId, long itemId, ItemDto item);

    ItemDtoWithComments findById(long userId, long itemId);

    List<ItemDto> searchText(String text, int from, int size);

    List<ItemDtoWithBookings> findByOwner(long userId, int from, int size);

    CommentDto createComment(CommentDto commentDto, long userId, long itemId);
}
