package ru.practicum.shareit.item.repository;

import java.util.List;

import ru.practicum.shareit.item.dto.ItemDto;

public interface ItemRepository {
    void create(ItemDto item);

    void update(ItemDto item);

    List<ItemDto> findByOwner(long userId);

    ItemDto findById(long itemId);

    List<ItemDto> findByText(String text);
}
