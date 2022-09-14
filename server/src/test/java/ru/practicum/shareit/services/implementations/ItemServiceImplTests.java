package ru.practicum.shareit.services.implementations;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingStartDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.service.UserService;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemServiceImplTests {
    private final EntityManager tem;
    private static UserDto userDto;
    private static ItemDto itemDto;
    private static CommentDto commentDto;
    private final ItemService itemService;
    private final UserService userService;
    private final BookingService bookingService;
    private static BookingStartDto bookingStartDto;

    @BeforeAll
    public static void beforeAll() {
        userDto = UserDto.builder()
                .email("test@ya.ru")
                .name("Тестировщик")
                .build();

        itemDto = ItemDto.builder()
                .name("Велосипед")
                .description("Шестиколесный")
                .available(true)
                .build();

        commentDto = CommentDto.builder()
                .text("Просто супер")
                .build();

        bookingStartDto = BookingStartDto.builder()
                .start(LocalDateTime.now().plusSeconds(1L))
                .end(LocalDateTime.now().plusSeconds(2L))
                .build();
    }

    @Test
    void createItem() {
        UserDto userToSave = userService.create(userDto);
        ItemDto itemToSave = itemService.create(userToSave.getId(), itemDto);
        TypedQuery<Item> query = tem.createQuery("Select i from Item i where i.id = :savedId", Item.class);

        Item item = query
                .setParameter("savedId", itemToSave.getId())
                .getSingleResult();
        assertThat(itemToSave.getId(), notNullValue());
        assertThat(item.getId(), equalTo(itemToSave.getId()));
        assertThat(item.getOwner(), equalTo(UserMapper.toUser(userToSave)));
        assertThat(item.getName(), equalTo(itemToSave.getName()));
        assertThat(item.getDescription(), equalTo(itemToSave.getDescription()));
    }

    @Test
    void createComment() throws InterruptedException {
        UserDto user = UserDto.builder()
                .name("Андрей")
                .email("andrew@ya.ru")
                .build();
        UserDto booker = userService.create(user);
        UserDto itemOwner = userService.create(userDto);
        ItemDto itemToSave = itemService.create(itemOwner.getId(), itemDto);
        BookingStartDto bookingStartDto = BookingStartDto.builder()
                .itemId(itemToSave.getId())
                .bookerId(booker.getId())
                .start(LocalDateTime.now().plusSeconds(1L))
                .end(LocalDateTime.now().plusSeconds(2L))
                .build();
        bookingStartDto.setItemId(itemToSave.getId());
        bookingStartDto.setBookerId(booker.getId());
        bookingService.create(booker.getId(), bookingStartDto);
        Thread.sleep(5000);
        CommentDto commentToSave = itemService.createComment(commentDto, booker.getId(), itemToSave.getId());

        TypedQuery<Comment> query = tem.createQuery("Select c from Comment c where c.id = :savedId", Comment.class);
        Comment comment = query
                .setParameter("savedId", commentToSave.getId())
                .getSingleResult();
        assertThat(commentToSave.getId(), notNullValue());
        assertThat(comment.getId(), equalTo(commentToSave.getId()));
        assertThat(comment.getAuthorName(), equalTo(UserMapper.toUser(booker)));
        assertThat(comment.getItem(), equalTo(ItemMapper.toItem(itemToSave, UserMapper.toUser(itemOwner))));
        assertThat(comment.getText(), equalTo(commentToSave.getText()));
        assertThat(comment.getCreated(), notNullValue());
    }

    @Test
    void update() {
        UserDto itemOwner = userService.create(userDto);
        ItemDto oldItem = itemService.create(itemOwner.getId(), itemDto);
        ItemDto updatedItem = ItemDto.builder()
                .name("Велосипед для дачи")
                .available(false)
                .build();

        ItemDto updatedAndReturned = itemService.update(itemOwner.getId(), oldItem.getId(), updatedItem);

        TypedQuery<Item> query = tem.createQuery("Select i from Item i where i.id = :savedId", Item.class);
        Item item = query
                .setParameter("savedId", oldItem.getId())
                .getSingleResult();

        System.out.println(item);
        assertThat(item.getId(), equalTo(updatedAndReturned.getId()));
        assertThat(item.getOwner(), equalTo(UserMapper.toUser(itemOwner)));
        assertThat(item.getName(), equalTo(updatedItem.getName()));
        assertThat(item.getAvailable(), equalTo(updatedItem.getAvailable()));
        assertThat(item.getDescription(), equalTo(oldItem.getDescription()));
    }

    @Test
    void findById() {
        UserDto itemOwner = userService.create(userDto);
        ItemDto itemToSave = itemService.create(itemOwner.getId(), itemDto);
        ItemDtoWithComments foundById = itemService.findById(itemOwner.getId(), itemToSave.getId());
        TypedQuery<Item> query = tem.createQuery("Select i from Item i where i.id = :savedId", Item.class);

        Item item = query
                .setParameter("savedId", itemToSave.getId())
                .getSingleResult();

        assertThat(foundById.getComments(), notNullValue());
        assertThat(item.getName(), equalTo(foundById.getName()));
        assertThat(item.getDescription(), equalTo(foundById.getDescription()));
    }

    @Test
    void findItemsByOwner() {
        UserDto itemOwner = userService.create(userDto);
        itemService.create(itemOwner.getId(), itemDto);
        UserDto itemOwnerTwo = UserDto.builder()
                .name("Валерий")
                .email("valera@ya.ru")
                .build();
        UserDto itemTwoAndThreeOwner = userService.create(itemOwnerTwo);
        ItemDto itemTwo = ItemDto.builder()
                .name("Велосипед STELS")
                .description("Дешево и сердито")
                .available(true)
                .build();
        ItemDto itemThree = ItemDto.builder()
                .name("Велосипед KHS")
                .description("Американская классика")
                .available(true)
                .build();

        ItemDto itemToSaveTwo = itemService.create(itemTwoAndThreeOwner.getId(), itemTwo);
        ItemDto itemToSaveThree = itemService.create(itemTwoAndThreeOwner.getId(), itemThree);
        List<ItemDtoWithBookings> items = itemService.findByOwner(itemTwoAndThreeOwner.getId(), 0, 10);
        assertThat(items.size(), equalTo(2));
        assertThat(items.get(0).getOwner(), equalTo(itemTwoAndThreeOwner.getId()));
        assertThat(items.get(1).getOwner(), equalTo(itemTwoAndThreeOwner.getId()));
        assertThat(items.get(0).getId(), equalTo(itemToSaveTwo.getId()));
        assertThat(items.get(1).getId(), equalTo(itemToSaveThree.getId()));
    }

    @Test
    void searchTest() {
        UserDto owner = userService.create(userDto);
        itemService.create(owner.getId(), itemDto);
        UserDto user = UserDto.builder()
                .name("Никита")
                .email("nikita@ya.ru")
                .build();
        UserDto ownerTwo = userService.create(user);
        ItemDto item = ItemDto.builder()
                .name("Велосипед синий")
                .description("Для вечерних прогулок")
                .available(true)
                .build();
        ItemDto itemTwo = ItemDto.builder()
                .name("Велосипед зеленый")
                .description("Очень красивый")
                .available(true)
                .build();

        ItemDto itemToSave = itemService.create(ownerTwo.getId(), item);
        itemService.create(ownerTwo.getId(), itemTwo);
        List<ItemDto> textFound = itemService.searchText("велосипед", 0, 10);
        assertThat(textFound.size(), equalTo(3));
        assertThat(textFound.get(0).getName(), equalTo(itemDto.getName()));
        assertThat(textFound.get(0).getDescription(), equalTo(itemDto.getDescription()));
        assertThat(textFound.get(1).getName(), equalTo(itemToSave.getName()));
        assertThat(textFound.get(1).getDescription(), equalTo(itemToSave.getDescription()));
    }
} 
