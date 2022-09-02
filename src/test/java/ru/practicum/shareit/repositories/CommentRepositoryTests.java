package ru.practicum.shareit.repositories;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class CommentRepositoryTests {
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private TestEntityManager tem;

    @Test
    void findCommentsByItem() {
        User user = User.builder()
                .name("Владислав")
                .email("vlad@ya.ru")
                .build();
        Item item = Item.builder()
                .name("Велосипед городской")
                .owner(user)
                .available(true)
                .description("Подходит для доставки")
                .build();
        Comment comment = Comment.builder()
                .item(item)
                .text("Идеально подошел")
                .authorName(user)
                .created(LocalDateTime.now())
                .build();

        tem.persist(user);
        tem.persist(item);
        tem.persist(comment);
        List<Comment> commentsList = commentRepository.findCommentsByItem_Id(item.getId());
        assertEquals(1, commentsList.size());
        assertThat(commentsList.get(0), equalTo(comment));
    }
}
