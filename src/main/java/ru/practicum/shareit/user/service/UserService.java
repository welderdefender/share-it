package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto create(UserDto user);

    UserDto update(long id, UserDto user);

    void remove(long id);

    UserDto findById(long id);

    List<UserDto> findAll();
}