package ru.practicum.shareit.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.errors.exceptions.DuplicateEmailException;
import ru.practicum.shareit.errors.exceptions.NullEmailException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private long id = 1;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDto create(UserDto user) {
        if (user.getEmail() == null) {
            throw new NullEmailException("Не указан Email");
        }
        checkForEmailDuplication(user);
        user.setId(id++);
        userRepository.create(user);
        log.info("Пользователь с id {} добавлен", user.getId());
        return user;
    }

    @Override
    public UserDto update(long id, UserDto user) {
        checkForEmailDuplication(user);
        UserDto userToUpdate = userRepository.findById(id);
        if (user.getName() != null) {
            userToUpdate.setName(user.getName());
        }
        if (user.getEmail() != null) {
            userToUpdate.setEmail(user.getEmail());
        }
        userRepository.update(userToUpdate);
        log.info("Пользователь с id {} добавлен", user.getId());
        return userToUpdate;
    }

    @Override
    public void remove(long id) {
        userRepository.remove(id);
        log.info("Пользователь с id {} удалён", id);
    }

    @Override
    public UserDto findById(long id) {
        return userRepository.findById(id);
    }

    @Override
    public List<UserDto> findAll() {
        return userRepository.findAll();
    }

    public void checkForEmailDuplication(UserDto user) {
        List<String> allEmails = userRepository.findAll().stream()
                .map(UserDto::getEmail)
                .collect(Collectors.toList());
        if (user.getEmail() != null) {
            for (String allEmail : allEmails) {
                if (user.getEmail().equals(allEmail)) {
                    throw new DuplicateEmailException("Пользователь с таким Email уже зарегистрирован");
                }
            }
        }
    }
}
