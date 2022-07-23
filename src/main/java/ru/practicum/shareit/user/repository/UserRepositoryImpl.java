package ru.practicum.shareit.user.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.*;

@Repository
public class UserRepositoryImpl implements UserRepository {
    private final Map<Long, UserDto> userList = new HashMap<>();

    @Override
    public void create(UserDto user) {
        userList.put(user.getId(), user);
    }

    @Override
    public void update(UserDto user) {
        userList.put(user.getId(), user);
    }

    @Override
    public void remove(long id) {
        userList.remove(id);
    }

    @Override
    public UserDto findById(long id) {
        return userList.get(id);
    }

    @Override
    public List<UserDto> findAll() {
        return new ArrayList<>(userList.values());
    }
}
