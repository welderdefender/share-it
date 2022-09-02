package ru.practicum.shareit.requests.service;

import ru.practicum.shareit.requests.dto.ItemRequestDto;
import ru.practicum.shareit.requests.dto.RequestDtoItems;
import ru.practicum.shareit.requests.dto.RequestShortDto;

import java.util.List;

public interface RequestService {
    ItemRequestDto create(long userId, RequestShortDto requestShortDto);

    List<RequestDtoItems> findByUserId(long userId);

    List<RequestDtoItems> findAllUsersRequests(long userId, int from, int size);

    RequestDtoItems findById(long userId, long requestId);
}
