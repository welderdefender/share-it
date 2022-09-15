package ru.practicum.shareit.repositories;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class UserRepositoryTests {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TestEntityManager tem;

    @Test
    void findById() {
        User user = User.builder()
                .name("Геннадий")
                .email("gena@ya.ru")
                .build();

        tem.persist(user);
        Optional<User> userFound = userRepository.findById(user.getId());
        assertTrue(userFound.isPresent());
        assertThat(userFound.get().getName(), equalTo(user.getName()));
        assertThat(userFound.get().getEmail(), equalTo(user.getEmail()));
    }

    @Test
    void checkIfUserExistsByEmail() {
        User user = new User();
        user.setName("Геннадий");
        user.setEmail("gena@mail.ru");
        tem.persist(user);
        Boolean doesExist = userRepository.existsUserByEmail("gena@mail.ru");
        assertThat(doesExist, equalTo(true));
    }
}