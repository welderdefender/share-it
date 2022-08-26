package ru.practicum.shareit.item.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.errors.exceptions.*;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    public ItemServiceImpl(ItemRepository itemRepository, UserRepository userRepository,
                           CommentRepository commentRepository, BookingRepository bookingRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    public ItemDto create(long userId, ItemDto itemDto) {
        checkIfUserExists(userId);
        Item item = ItemMapper.toItem(itemDto, userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с таким id не найден")));
        itemRepository.save(item);
        log.info("Пользователь {} добавил новую вещь с id {}", userId, item.getId());
        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto update(long userId, long itemId, ItemDto itemDto) {
        checkIfUserExists(userId);
        Item itemToUpdate = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Вещь с таким id не найдена"));

        if (userId != itemToUpdate.getOwner().getId())
            throw new NoAccessException("У пользователя нет доступа к этой вещи");
        if (itemDto.getAvailable() != null) itemToUpdate.setAvailable(itemDto.getAvailable());
        if (itemDto.getName() != null) itemToUpdate.setName(itemDto.getName());
        if (itemDto.getDescription() != null) itemToUpdate.setDescription(itemDto.getDescription());

        itemRepository.save(itemToUpdate);
        log.info("Пользователь {} обновил информацию о вещи с id {}", userId, itemId);
        return ItemMapper.toItemDto(itemToUpdate);
    }

    @Override
    public ItemDtoWithComments findById(long userId, long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Вещь с таким id не найдена"));

        List<CommentDto> comment = commentRepository.findCommentsByItem_Id(itemId).stream()
                .map(CommentsMapper::toCommentDto)
                .collect(Collectors.toList());

        return item.getOwner().getId() == userId ? ItemMapper.toItemDtoWithComments(item, getLastBooking(itemId),
                getNextBooking(itemId), comment) : ItemMapper.toItemDtoWithComments(item, null, null, comment);
    }

    @Override
    public List<ItemDtoWithBookings> findByOwner(long userId) {
        return itemRepository.findItemsByOwnerId(userId).stream()
                .map(item -> getItemWithBooking(item.getId()))
                .collect(Collectors.toList());
    }

    private ItemDtoWithBookings getItemWithBooking(long itemId) {
        Item itemToFind = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Вещь с таким id не найдена"));
        return ItemMapper.toItemDtoWithBookings(itemToFind, getLastBooking(itemId), getNextBooking(itemId));
    }

    @Override
    public List<ItemDto> searchText(String text) {
        if (text.isBlank() || text.isEmpty()) return new ArrayList<>();
        return itemRepository.searchText(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    private void checkIfUserExists(long userId) {
        if (!userRepository.existsById(userId))
            throw new UserNotFoundException("Пользователь не найден");
    }

    @Override
    public CommentDto createComment(CommentDto commentDto, long userId, long itemId) {
        checkCommentNotEmpty(commentDto);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Комментарий с таким id не найден"));
        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с таким id не найден"));
        Optional<Booking> booking = bookingRepository.getTopByItem_IdAndBooker_IdOrderByEndAsc(itemId, userId);

        if (booking.isEmpty() || booking.get().getEnd().isAfter(LocalDateTime.now()))
            throw new BadRequestException("Этот пользователь не может оставить комментарий");
        commentDto.setCreated(LocalDateTime.now());

        Comment comment = commentRepository.save(CommentsMapper.toComment(commentDto, item, creator));
        log.info("Пользователь с id={} добавил комментарий к вещи с id={}", userId, itemId);
        return CommentsMapper.toCommentDto(comment);
    }

    private void checkCommentNotEmpty(CommentDto commentDto) {
        if (commentDto.getText().isBlank()) throw new BadRequestException("Комментарий не может быть пустым");
    }

    private BookingDto getLastBooking(long itemId) {
        Optional<Booking> lastBooking = bookingRepository.getTopByItem_IdAndEndBeforeOrderByStartDesc(itemId,
                LocalDateTime.now());
        return lastBooking.isEmpty() ? null : BookingMapper.toBookingDto(lastBooking.get());
    }

    private BookingDto getNextBooking(long itemId) {
        Optional<Booking> nextBooking = bookingRepository.getTopByItem_IdAndStartAfterOrderByStartDesc(itemId,
                LocalDateTime.now());
        return nextBooking.isEmpty() ? null : BookingMapper.toBookingDto(nextBooking.get());
    }
}