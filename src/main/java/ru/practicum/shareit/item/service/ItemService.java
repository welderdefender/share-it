package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto create(long userId, ItemDto item);

    ItemDto update(long userId, long itemId, ItemDto item);

    ItemDto findById(long itemId);

    List<ItemDto> findByText(String text);

    List<ItemDto> findByOwner(long userId);
}
