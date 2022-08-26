package ru.practicum.shareit.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.errors.exceptions.DuplicateEmailException;
import ru.practicum.shareit.errors.exceptions.UserNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDto create(UserDto user) {
        User userDto = userRepository.save(UserMapper.toUser(user));
        log.info("Пользователь с id {} добавлен", user.getId());
        return UserMapper.userDto(userDto);
    }

    @Override
    public UserDto update(long id, UserDto user) {
        User userToUpdate = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с таким id не найден"));
        if (user.getEmail() != null) {
            checkForEmailDuplication(user.getEmail());
            userToUpdate.setEmail(user.getEmail());
        }
        if (user.getName() != null) {
            userToUpdate.setName(user.getName());
        }
        userRepository.save(userToUpdate);
        log.info("Пользователь успешно обновлён!");
        return UserMapper.userDto(userToUpdate);
    }

    @Override
    public void remove(long id) {
        userRepository.deleteById(id);
        log.info("Пользователь с id {} удалён", id);
    }

    @Override
    public UserDto findById(long id) {
        return UserMapper.userDto(userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с таким id не найден")));
    }

    @Override
    public List<UserDto> findAll() {
        return userRepository.findAll().stream()
                .map(UserMapper::userDto)
                .collect(Collectors.toList());
    }

    public void checkForEmailDuplication(String email) {
        if (userRepository.existsUserByEmail(email))
            throw new DuplicateEmailException("Пользователь с таким Email уже зарегистрирован");
    }
}
