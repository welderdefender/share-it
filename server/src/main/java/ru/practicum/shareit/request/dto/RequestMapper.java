package ru.practicum.shareit.request.dto;

import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class RequestMapper {
    public static ItemRequestDto toRequestDto(Request request) {
        return ItemRequestDto.builder()
                .id(request.getId())
                .userId(request.getUser().getId())
                .description(request.getDescription())
                .created(request.getCreationTime())
                .build();
    }

    public static Request toRequest(RequestShortDto requestShortDto, LocalDateTime localDateTime, User user) {
        return Request.builder()
                .id(requestShortDto.getId())
                .description(requestShortDto.getDescription())
                .creationTime(localDateTime)
                .user(user)
                .build();
    }

    public static RequestDtoItems toRequestDtoItems(Request request, List<Item> items) {
        return RequestDtoItems.builder()
                .id(request.getId())
                .description(request.getDescription())
                .created(request.getCreationTime())
                .items(items.stream()
                        .map(ItemMapper::toItemDto)
                        .collect(Collectors.toList()))
                .build();
    }
}