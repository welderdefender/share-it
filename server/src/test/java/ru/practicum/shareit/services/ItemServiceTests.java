package ru.practicum.shareit.services;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.errors.exceptions.BadRequestException;
import ru.practicum.shareit.errors.exceptions.ItemNotFoundException;
import ru.practicum.shareit.errors.exceptions.UserNotFoundException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.pagination.Pagination;
import ru.practicum.shareit.request.repository.RequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTests {
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private RequestRepository requestRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private ItemServiceImpl itemService;
    private static Item item;
    private static User user;
    private static CommentDto comment;
    private static ItemDto itemDto;

    @BeforeAll
    public static void beforeAll() {
        itemDto = ItemDto.builder()
                .id(1L)
                .name("велосипед")
                .description("С гидромассажем")
                .available(true)
                .owner(1L)
                .build();

        user = User.builder()
                .id(1L)
                .name("Влад")
                .email("vlad@ya.ru")
                .build();

        item = ItemMapper.toItem(itemDto, user);
        comment = CommentDto.builder().text("женский").build();
    }

    @Test
    void ifCommentPostedOnNotExistingBookingThenBadRequestException() {
        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        Mockito.when(itemRepository.findById(1L))
                .thenReturn(Optional.of(item));
        Mockito.when(bookingRepository.getTopByItem_IdAndBooker_IdOrderByEndAsc(1L, 1L))
                .thenReturn(Optional.empty());

        final BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> itemService.createComment(comment, 1L, 1L));

        assertEquals("Этот пользователь не может оставить комментарий",
                exception.getMessage());

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
        Mockito.verify(itemRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
        Mockito.verify(bookingRepository, Mockito.times(1))
                .getTopByItem_IdAndBooker_IdOrderByEndAsc(Mockito.anyLong(), Mockito.anyLong());
        Mockito.verify(commentRepository, Mockito.never())
                .save(Mockito.any());
    }

    @Test
    void ifPostingCommentIsOkThenSaveCommentRepository() {
        Booking prevBooking = Booking.builder().end(LocalDateTime.now().minusDays(1L)).build();
        Comment commentIsCorrect = CommentsMapper.toComment(comment, item, user);
        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        Mockito.when(itemRepository.findById(1L))
                .thenReturn(Optional.of(item));
        Mockito.when(bookingRepository.getTopByItem_IdAndBooker_IdOrderByEndAsc(1L, 1L))
                .thenReturn(Optional.of(prevBooking));
        Mockito.when(commentRepository.save(Mockito.any()))
                .thenReturn(commentIsCorrect);

        CommentDto commentDto = itemService.createComment(comment, 1L, 1L);
        assertThat(commentDto, equalTo(CommentsMapper.toCommentDto(commentIsCorrect)));
        Mockito.verify(userRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
        Mockito.verify(itemRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
        Mockito.verify(bookingRepository, Mockito.times(1))
                .getTopByItem_IdAndBooker_IdOrderByEndAsc(Mockito.anyLong(), Mockito.anyLong());
        Mockito.verify(commentRepository, Mockito.times(1))
                .save(Mockito.any());
    }

    @Test
    void ifUpdatingByNotExistingUserThenUserNotFoundException() {
        Mockito.when(userRepository.existsById(1L))
                .thenReturn(false);

        final UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> itemService.update(1L, 1L, itemDto));

        assertEquals("Пользователь не найден", exception.getMessage());
        Mockito.verify(userRepository, Mockito.times(1))
                .existsById(Mockito.anyLong());
        Mockito.verify(itemRepository, Mockito.never())
                .findById(Mockito.anyLong());
        Mockito.verify(itemRepository, Mockito.never())
                .save(Mockito.any());
    }

    @Test
    void ifUpdateDoesNotExistsThenItemNotFoundException() {
        Mockito.when(userRepository.existsById(1L))
                .thenReturn(true);
        Mockito.when(itemRepository.findById(1L))
                .thenThrow(new ItemNotFoundException("Вещи с таким id не существует"));

        final ItemNotFoundException exception = assertThrows(
                ItemNotFoundException.class,
                () -> itemService.update(1L, 1L, itemDto));

        assertEquals("Вещи с таким id не существует", exception.getMessage());
        Mockito.verify(userRepository, Mockito.times(1))
                .existsById(Mockito.anyLong());
        Mockito.verify(itemRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
        Mockito.verify(itemRepository, Mockito.never())
                .save(Mockito.any());
    }

    @Test
    void ifUpdatingItemIsOkThenCallSaveItemRepository() {
        Mockito.when(userRepository.existsById(1L))
                .thenReturn(true);
        Mockito.when(itemRepository.findById(1L))
                .thenReturn(Optional.of(item));
        Mockito.when(itemRepository.findById(1L))
                .thenReturn(Optional.of(item));

        ItemDto itemToReturn = itemService.update(1L, 1L, itemDto);
        assertThat(itemToReturn, equalTo(ItemMapper.toItemDto(item)));
        Mockito.verify(userRepository, Mockito.times(1))
                .existsById(Mockito.anyLong());
        Mockito.verify(itemRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
        Mockito.verify(itemRepository, Mockito.times(1))
                .save(Mockito.any());
    }

    @Test
    void ifItemIdWasNotFoundThenItemNotFoundException() {
        Mockito.when(itemRepository.findById(1L))
                .thenThrow(new ItemNotFoundException("Вещи с таким id не существует"));

        final ItemNotFoundException exception = assertThrows(
                ItemNotFoundException.class,
                () -> itemService.findById(1L, 1L));

        assertEquals("Вещи с таким id не существует", exception.getMessage());
        Mockito.verify(itemRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
        Mockito.verify(commentRepository, Mockito.never())
                .findCommentsByItem_Id(Mockito.anyLong());
        Mockito.verify(bookingRepository, Mockito.never())
                .getTopByItem_IdAndEndBeforeOrderByStartDesc(Mockito.anyLong(), Mockito.any(LocalDateTime.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .getTopByItem_IdAndStartAfterOrderByStartDesc(Mockito.anyLong(), Mockito.any(LocalDateTime.class));
    }

    @Test
    void ifUserIsOwnerOfItemFoundByIdThenReturnItemWithBookingsAnaDates() {
        List<Comment> commentsList = List.of(CommentsMapper.toComment(comment, item, user));
        Booking prev = Booking.builder().item(item).booker(user).start(LocalDateTime.now().minusDays(1)).build();
        Booking next = Booking.builder().item(item).booker(user).start(LocalDateTime.now().plusDays(1)).build();
        User owner = User.builder().id(1L).build();
        item.setOwner(owner);

        Mockito.when(itemRepository.findById(1L))
                .thenReturn(Optional.of(item));
        Mockito.when(commentRepository.findCommentsByItem_Id(1L))
                .thenReturn(commentsList);
        Mockito.when(bookingRepository.getTopByItem_IdAndEndBeforeOrderByStartDesc(Mockito.anyLong(),
                        Mockito.any(LocalDateTime.class)))
                .thenReturn(Optional.of(prev));
        Mockito.when(bookingRepository.getTopByItem_IdAndStartAfterOrderByStartDesc(Mockito.anyLong(),
                        Mockito.any(LocalDateTime.class)))
                .thenReturn(Optional.of(next));

        ItemDtoWithComments itemsReturned = itemService.findById(1L, 1L);
        assertNotNull(itemsReturned.getLastBooking());
        assertNotNull(itemsReturned.getNextBooking());
        BookingDto prevShortBooking = BookingMapper.toBookingDto(prev);
        BookingDto nextShortBooking = BookingMapper.toBookingDto(next);
        CommentDto comment = CommentsMapper.toCommentDto(commentsList.get(0));
        assertThat(itemsReturned, equalTo(ItemMapper.toItemDtoWithComments(item, prevShortBooking, nextShortBooking,
                List.of(comment))));

        Mockito.verify(itemRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
        Mockito.verify(commentRepository, Mockito.times(1))
                .findCommentsByItem_Id(Mockito.anyLong());
        Mockito.verify(bookingRepository, Mockito.times(1))
                .getTopByItem_IdAndEndBeforeOrderByStartDesc(Mockito.anyLong(), Mockito.any(LocalDateTime.class));
        Mockito.verify(bookingRepository, Mockito.times(1))
                .getTopByItem_IdAndStartAfterOrderByStartDesc(Mockito.anyLong(), Mockito.any(LocalDateTime.class));
    }

    @Test
    void ifUserIsNotOwnerOfItemThenReturnItemBookingsDateNull() {
        List<Comment> commentsList = List.of(CommentsMapper.toComment(comment, item, user));
        User owner = User.builder().id(5L).build();
        item.setOwner(owner);
        Mockito.when(itemRepository.findById(1L))
                .thenReturn(Optional.of(item));
        Mockito.when(commentRepository.findCommentsByItem_Id(1L))
                .thenReturn(commentsList);

        ItemDtoWithComments itemReturned = itemService.findById(1L, 1L);
        assertNull(itemReturned.getNextBooking());
        assertNull(itemReturned.getLastBooking());
        CommentDto comment = CommentsMapper.toCommentDto(commentsList.get(0));
        assertThat(itemReturned, equalTo(ItemMapper.toItemDtoWithComments(item, null, null, List.of(comment))));
        Mockito.verify(itemRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
        Mockito.verify(commentRepository, Mockito.times(1))
                .findCommentsByItem_Id(Mockito.anyLong());
        Mockito.verify(bookingRepository, Mockito.never())
                .getTopByItem_IdAndEndBeforeOrderByStartDesc(Mockito.anyLong(), Mockito.any(LocalDateTime.class));
        Mockito.verify(bookingRepository, Mockito.never())
                .getTopByItem_IdAndStartAfterOrderByStartDesc(Mockito.anyLong(), Mockito.any(LocalDateTime.class));
    }

    @Test
    void findItemsByOwnerIdThenReturnItemWithBookingsDate() {
        Slice<Item> items = new SliceImpl<>(List.of(item));
        Booking prev = Booking.builder().item(item).booker(user).start(LocalDateTime.now().minusDays(1)).build();
        Booking next = Booking.builder().item(item).booker(user).start(LocalDateTime.now().plusDays(1)).build();
        Mockito.when(itemRepository.findItemsByOwnerId(Mockito.anyLong(), Mockito.any(Pagination.class)))
                .thenReturn(items);
        Mockito.when(bookingRepository.getTopByItem_IdAndEndBeforeOrderByStartDesc(Mockito.anyLong(),
                        Mockito.any(LocalDateTime.class)))
                .thenReturn(Optional.of(prev));
        Mockito.when(bookingRepository.getTopByItem_IdAndStartAfterOrderByStartDesc(Mockito.anyLong(),
                        Mockito.any(LocalDateTime.class)))
                .thenReturn(Optional.of(next));

        List<ItemDtoWithBookings> returnedItemsList = itemService.findByOwner(1L, 0, 10);
        assertNotNull(returnedItemsList.get(0).getLastBooking());
        assertNotNull(returnedItemsList.get(0).getNextBooking());
        BookingDto prevShortBooking = BookingMapper.toBookingDto(prev);
        BookingDto nextShortBooking = BookingMapper.toBookingDto(next);
        assertThat(returnedItemsList.get(0), equalTo(ItemMapper.toItemDtoWithBookings(item, prevShortBooking, nextShortBooking)));
        Mockito.verify(itemRepository, Mockito.times(1))
                .findItemsByOwnerId(1L, Pagination.of(0, 10, Sort.by("id").ascending()));
        Mockito.verify(bookingRepository, Mockito.times(1))
                .getTopByItem_IdAndEndBeforeOrderByStartDesc(Mockito.anyLong(), Mockito.any(LocalDateTime.class));
        Mockito.verify(bookingRepository, Mockito.times(1))
                .getTopByItem_IdAndStartAfterOrderByStartDesc(Mockito.anyLong(), Mockito.any(LocalDateTime.class));
    }

    @Test
    void ifTextToFindIsBlankThenReturnEmptyList() {
        List<ItemDto> emptyList = itemService.searchText(" ", 0, 10);
        assertThat(emptyList.size(), equalTo(0));
        Mockito.verify(itemRepository, Mockito.never())
                .searchText(" ", PageRequest.of(0, 10));
    }

    @Test
    void ifTextToFindIsOkThenCallSearchTextItemRepository() {
        Slice<Item> items = new SliceImpl<>(List.of(item));
        Mockito.when(itemRepository.searchText(Mockito.anyString(), Mockito.any(Pageable.class)))
                .thenReturn(items);

        List<ItemDto> returnedList = itemService.searchText("велосипед", 0, 10);
        assertThat(returnedList.size(), equalTo(1));
        Mockito.verify(itemRepository, Mockito.times(1))
                .searchText(Mockito.anyString(), Mockito.any(Pageable.class));
    }
} 
