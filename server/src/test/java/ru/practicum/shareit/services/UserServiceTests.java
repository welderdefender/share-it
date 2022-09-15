package ru.practicum.shareit.services;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.errors.exceptions.DuplicateEmailException;
import ru.practicum.shareit.errors.exceptions.UserNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class UserServiceTests {
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserServiceImpl userService;
    private static UserDto userDto;
    private static User user;

    @BeforeAll
    public static void beforeAll() {
        userDto = UserDto.builder()
                .id(1L)
                .name("Татьяна")
                .email("tatyana@ya.ru")
                .build();

        user = UserMapper.toUser(userDto);
    }

    @Test
    void ifCreateThenCallCreateRepository() {
        Mockito
                .when(userRepository.save(Mockito.any()))
                .thenReturn(user);

        UserDto userReturned = userService.create(userDto);
        assertThat(userReturned, equalTo(UserMapper.userDto(user)));
        Mockito.verify(userRepository, Mockito.times(1))
                .save(Mockito.any());
    }

    @Test
    void ifUpdateUserWithNoEmailThenDoNotCallExistsByEmailRepository() {
        UserDto userWithoutEmail = UserDto.builder().id(1L).name("Татьяна").build();
        Mockito.when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user));
        Mockito.when(userRepository.save(Mockito.any()))
                .thenReturn(user);

        UserDto userToUpdate = userService.update(1L, userWithoutEmail);
        assertThat(userToUpdate, equalTo(UserMapper.userDto(user)));
        assertThat(userToUpdate.getId(), equalTo(user.getId()));
        assertThat(userToUpdate.getEmail(), equalTo(user.getEmail()));
        assertThat(userToUpdate.getName(), equalTo("Татьяна"));

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
        Mockito.verify(userRepository, Mockito.times(1))
                .save(user);
        Mockito.verify(userRepository, Mockito.never())
                .existsUserByEmail(Mockito.anyString());
    }

    @Test
    void ifUpdateUserWithCorrectEmailThenCallThreeRepositoryMethods() {
        userDto.setEmail("valera@ya.ru");
        Mockito.when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user));
        Mockito.when(userRepository.existsUserByEmail(Mockito.anyString()))
                .thenReturn(false);
        Mockito.when(userRepository.save(Mockito.any()))
                .thenReturn(user);

        UserDto userToUpdate = userService.update(1L, userDto);
        assertThat(userToUpdate, equalTo(UserMapper.userDto(user)));
        assertThat(userToUpdate.getId(), equalTo(user.getId()));
        assertThat(userToUpdate.getEmail(), equalTo("valera@ya.ru"));
        assertThat(userToUpdate.getName(), equalTo(user.getName()));

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
        Mockito.verify(userRepository, Mockito.times(1))
                .existsUserByEmail(Mockito.anyString());
        Mockito.verify(userRepository, Mockito.times(1))
                .save(user);
    }

    @Test
    void ifUpdateUserWhoDoesNotExistsThenThrowUserNotFoundException() {
        Mockito.when(userRepository.findById(1L))
                .thenThrow(new UserNotFoundException("Пользователь с таким id не найден"));

        final UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userService.update(1L, userDto));

        assertEquals("Пользователь с таким id не найден", exception.getMessage());
    }

    @Test
    void ifUpdateUserWithDuplicateEmailThenThrowDuplicateEmailException() {
        Mockito.when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user));
        Mockito.when(userRepository.existsUserByEmail("tatyana@ya.ru"))
                .thenThrow(new DuplicateEmailException("Пользователь с таким Email уже существует"));

        final DuplicateEmailException exception = assertThrows(
                DuplicateEmailException.class,
                () -> userService.update(1L, userDto));

        assertEquals("Пользователь с таким Email уже существует", exception.getMessage());
    }

    @Test
    void ifFindByIdUserExistsThenReturnUser() {
        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        UserDto userToReturn = userService.findById(1L);
        assertThat(userToReturn, equalTo(UserMapper.userDto(user)));
        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1L);
    }

    @Test
    void ifFindByIdUserDoesNotExistsThenThrowUserNotFoundException() {
        Mockito.when(userRepository.findById(5L))
                .thenThrow(new UserNotFoundException("Пользователя с таким id не существует"));

        final UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userService.findById(5L));

        assertEquals("Пользователя с таким id не существует", exception.getMessage());
        Mockito.verify(userRepository, Mockito.times(1))
                .findById(5L);
    }

    @Test
    void ifFindAllThenCallRepositoryFindAllMethod() {
        Mockito.when(userRepository.findAll())
                .thenReturn(List.of(user));

        List<UserDto> usersList = userService.findAll();
        assertThat(usersList.size(), equalTo(1));
        assertThat(usersList.get(0), equalTo(UserMapper.userDto(user)));
        Mockito.verify(userRepository, Mockito.times(1))
                .findAll();
    }

    @Test
    void ifDeleteThenCallRepositoryDeleteMethod() {
        userService.remove(1L);
        Mockito.verify(userRepository, Mockito.times(1))
                .deleteById(1L);
    }
}
