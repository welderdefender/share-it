package ru.practicum.shareit.repositories;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Slice;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.pagination.Pagination;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ItemRepositoryTests {
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private TestEntityManager em;

    @Test
    void findItemsByOwner() {
        User user = User.builder()
                .name("Дмитрий")
                .email("dmitry@mail.com")
                .build();
        Item first = Item.builder()
                .name("Велосипед 1")
                .owner(user)
                .available(true)
                .description("женский")
                .build();
        Item second = Item.builder()
                .name("Велосипед 2")
                .owner(user)
                .available(true)
                .description("детский")
                .build();

        em.persist(user);
        em.persist(first);
        em.persist(second);
        Slice<Item> items = itemRepository.findItemsByOwnerId(user.getId(), Pagination.of(0, 5));
        assertEquals(2, items.getContent().size());
        assertThat(items.getContent().get(0), equalTo(first));
        assertThat(items.getContent().get(1), equalTo(second));
    }

    @Test
    void checkIfExistsByOwner() {
        User user = User.builder()
                .name("Дмитрий")
                .email("dmitry@mail.com")
                .build();
        Item first = Item.builder()
                .name("Велосипед 1")
                .owner(user)
                .available(true)
                .description("женский")
                .build();

        em.persist(user);
        em.persist(first);
        boolean exists = itemRepository.existsByOwnerId(user.getId());
        assertTrue(exists);
    }

    @Test
    void findItemsByRequest() {
        User user = User.builder()
                .name("Дмитрий")
                .email("dmitry@mail.com")
                .build();
        Request request = Request.builder()
                .user(user)
                .creationTime(LocalDateTime.now())
                .description("Мне нужен такой")
                .build();
        Item first = Item.builder()
                .name("Велосипед 1")
                .owner(user)
                .available(true)
                .description("женский")
                .request(request)
                .build();
        Item second = Item.builder()
                .name("Велосипед 2")
                .owner(user)
                .available(true)
                .description("детский")
                .request(request)
                .build();

        em.persist(user);
        em.persist(request);
        em.persist(first);
        em.persist(second);
        List<Item> items = itemRepository.findItemsByRequestId(request.getId());
        assertEquals(2, items.size());
        assertThat(items.get(0), equalTo(first));
        assertThat(items.get(1), equalTo(second));
    }
}
