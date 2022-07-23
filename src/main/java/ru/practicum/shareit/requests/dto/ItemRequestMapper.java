package ru.practicum.shareit.requests.dto;

import ru.practicum.shareit.requests.ItemRequest;
import ru.practicum.shareit.user.model.User;

public class ItemRequestMapper {

    public static ItemRequestDto itemRequestDto(ItemRequest itemRequest) {
        return ItemRequestDto.builder()
                .id(itemRequest.getId())
                .requester(itemRequest.getRequester().getId())
                .description(itemRequest.getDescription())
                .created(itemRequest.getCreated())
                .build();
    }

    public static ItemRequest toItemRequest(ItemRequestDto itemRequestDto, User user) {
        return ItemRequest.builder()
                .id(itemRequestDto.getId())
                .requester(user)
                .description(itemRequestDto.getDescription())
                .created(itemRequestDto.getCreated())
                .build();
    }
}