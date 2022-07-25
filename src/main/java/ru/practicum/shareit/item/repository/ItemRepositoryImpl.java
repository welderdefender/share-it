package ru.practicum.shareit.item.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class ItemRepositoryImpl implements ItemRepository {
    private final Map<Long, ItemDto> itemsList = new HashMap<>();

    @Override
    public void create(ItemDto item) {
        itemsList.put(item.getId(), item);
    }

    @Override
    public void update(ItemDto item) {
        itemsList.put(item.getId(), item);
    }

    @Override
    public ItemDto findById(long itemId) {
        return itemsList.get(itemId);
    }

    @Override
    public List<ItemDto> findByOwner(long userId) {
        return itemsList.values().stream()
                .filter(item -> item.getOwner() == userId)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> findByText(String text) {
        if (text.isEmpty()) return new ArrayList<>();
        final String textToFind = text.trim().toLowerCase();
        return itemsList.values().stream()
                .filter(item -> item.getName().toLowerCase().contains(textToFind)
                        || item.getDescription().toLowerCase().contains(textToFind))
                .filter(ItemDto::getAvailable)
                .collect(Collectors.toList());
    }
}
