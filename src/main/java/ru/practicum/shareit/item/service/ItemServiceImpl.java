package ru.practicum.shareit.item.service;

import ru.practicum.shareit.errors.exceptions.BadRequestException;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

import ru.practicum.shareit.errors.exceptions.UserNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ItemServiceImpl implements ItemService {
    private long id;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    public ItemServiceImpl(ItemRepository itemRepository, UserRepository userRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    @Override
    public ItemDto create(long userId, ItemDto item) {
        checkIfUserExists(userId);
        if (item.getName().isEmpty() || item.getDescription() == null || item.getAvailable() == null) {
            throw new BadRequestException("Вы оставили пустым название, описание или доступность");
        }
        item.setOwner(userId);
        item.setId(++id);
        item.setAvailable(true);
        itemRepository.create(item);
        log.info("Пользователь {} добавил новую вещь с id {}", userId, item.getId());
        return item;
    }

    @Override
    public ItemDto update(long userId, long itemId, ItemDto item) {
        checkIfUserExists(userId);
        ItemDto itemToUpdate = itemRepository.findById(itemId);
        if (itemToUpdate.getOwner() != userId)
            throw new UserNotFoundException("Это чужая вещь");
        if (item.getName() != null) itemToUpdate.setName(item.getName());
        if (item.getDescription() != null) itemToUpdate.setDescription(item.getDescription());
        if (item.getAvailable() != null) itemToUpdate.setAvailable(item.getAvailable());
        itemRepository.update(itemToUpdate);
        log.info("Пользователь {} обновил информацию о вещи с id {}", userId, itemId);
        return itemToUpdate;
    }

    @Override
    public ItemDto findById(long itemId) {
        return itemRepository.findById(itemId);
    }

    @Override
    public List<ItemDto> findByOwner(long userId) {
        return itemRepository.findByOwner(userId);
    }

    @Override
    public List<ItemDto> findByText(String text) {
        if (text.isBlank() || text.isEmpty()) return new ArrayList<>();
        return itemRepository.findByText(text);
    }

    private void checkIfUserExists(long userId) {
        if (userRepository.findById(userId) == null)
            throw new UserNotFoundException("Пользователь не найден");
    }
}