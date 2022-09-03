package ru.practicum.shareit.repositories;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Slice;
import ru.practicum.shareit.pagination.Pagination;
import ru.practicum.shareit.requests.model.Request;
import ru.practicum.shareit.requests.repository.RequestRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class RequestRepositoryTests {
    @Autowired
    private RequestRepository requestRepository;
    @Autowired
    private TestEntityManager tem;

    @Test
    void findAllByUserIdOrderByCreationTimeDesc() {
        User author = User.builder()
                .name("Дарья")
                .email("darya@hotmail.com")
                .build();
        User another = User.builder()
                .name("Мария")
                .email("maria@hotmail.com")
                .build();
        Request one = Request.builder()
                .user(author)
                .description("Описание1")
                .creationTime(LocalDateTime.now())
                .build();
        Request two = Request.builder()
                .user(another)
                .description("Описание2")
                .creationTime(LocalDateTime.now())
                .build();
        Request three = Request.builder()
                .user(author)
                .description("Описание3")
                .creationTime(LocalDateTime.now())
                .build();

        tem.persist(author);
        tem.persist(another);
        tem.persist(one);
        tem.persist(two);
        tem.persist(three);
        List<Request> requestsList = requestRepository.findAllByUserIdOrderByCreationTimeDesc(author.getId());
        assertEquals(2, requestsList.size());
        assertThat(requestsList.get(2), equalTo(one));
        assertThat(requestsList.get(1), equalTo(three));
    }

    @Test
    void findOtherUsersRequests() {
        User author = User.builder()
                .name("Дарья")
                .email("darya@hotmail.com")
                .build();
        User another = User.builder()
                .name("Мария")
                .email("maria@hotmail.com")
                .build();
        Request one = Request.builder()
                .user(author)
                .description("Описание1")
                .creationTime(LocalDateTime.now())
                .build();
        Request two = Request.builder()
                .user(another)
                .description("Описание2")
                .creationTime(LocalDateTime.now())
                .build();
        Request three = Request.builder()
                .user(author)
                .description("Описание3")
                .creationTime(LocalDateTime.now())
                .build();

        tem.persist(author);
        tem.persist(another);
        tem.persist(one);
        tem.persist(two);
        tem.persist(three);
        Slice<Request> requestsList = requestRepository.findAllOtherUsersRequests(author.getId(), Pagination.of(0, 5));
        assertEquals(1, requestsList.getContent().size());
        assertThat(requestsList.getContent().get(0), equalTo(two));
    }
}
