package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserRepository {
    void create(UserDto user);
    void update(UserDto user);
    void remove(long id);
    UserDto findById(long id);
    List<UserDto> findAll();
}
