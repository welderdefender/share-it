package ru.practicum.shareit.services.implementations;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserServiceImplTests {
    private final UserService userService;
    private static UserDto userDto;
    private final EntityManager tem;

    @BeforeAll
    public static void beforeAll() {
        userDto = UserDto.builder()
                .email("test@ya.ru")
                .name("Тестировщик")
                .build();
    }

    @Test
    void create() {
        UserDto userToSave = userService.create(userDto);
        TypedQuery<User> query = tem.createQuery("Select u from User u where u.email = :email", User.class);
        User user = query
                .setParameter("email", userDto.getEmail())
                .getSingleResult();
        assertThat(userToSave.getId(), notNullValue());
        assertThat(user.getId(), equalTo(userToSave.getId()));
        assertThat(user.getName(), equalTo(userDto.getName()));
        assertThat(user.getEmail(), equalTo(userDto.getEmail()));
    }

    @Test
    void update() {
        UserDto userToSave = userService.create(userDto);
        UserDto userToUpdate = UserDto.builder()
                .name("Александр")
                .email("aleks@ya.ru")
                .build();

        userService.update(userToSave.getId(), userToUpdate);
        TypedQuery<User> query = tem.createQuery("Select u from User u where u.id = :initId", User.class);
        User user = query
                .setParameter("initId", userToSave.getId())
                .getSingleResult();

        assertThat(user.getName(), equalTo(userToUpdate.getName()));
        assertThat(user.getEmail(), equalTo(userToUpdate.getEmail()));
    }

    @Test
    void delete() {
        UserDto userToSave = userService.create(userDto);
        assertThat(userToSave.getId(), equalTo(17L));
        userService.remove(17L);
        User user = tem.find(User.class, 17L);
        assertNull(user);
    }

    @Test
    void findAll() {
        UserDto userTwo = UserDto.builder()
                .name("Оксана")
                .email("oksana@ya.ru")
                .build();
        UserDto userThree = UserDto.builder()
                .name("Валерия")
                .email("lera@ya.ru")
                .build();

        userService.create(userDto);
        userService.create(userTwo);
        userService.create(userThree);
        List<UserDto> userList = userService.findAll();
        TypedQuery<User> query = tem.createQuery("Select u from User u", User.class);
        List<User> users = query.getResultList();
        assertThat(userList.size(), equalTo(3));
        assertThat(userList.get(0).getEmail(), equalTo(users.get(0).getEmail()));
        assertThat(userList.get(0).getName(), equalTo(users.get(0).getName()));
        assertThat(userList.get(2).getEmail(), equalTo(users.get(2).getEmail()));
        assertThat(userList.get(2).getName(), equalTo(users.get(2).getName()));
    }

    @Test
    void findById() {
        UserDto userToSave = userService.create(userDto);
        UserDto userToFind = userService.findById(userToSave.getId());
        TypedQuery<User> query = tem.createQuery("Select u from User u where u.id = :savedId", User.class);
        User user = query
                .setParameter("savedId", userToSave.getId())
                .getSingleResult();

        assertThat(user.getName(), equalTo(userToFind.getName()));
        assertThat(user.getEmail(), equalTo(userToFind.getEmail()));
    }
} 
