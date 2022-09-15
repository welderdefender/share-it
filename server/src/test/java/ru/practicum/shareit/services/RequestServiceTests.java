package ru.practicum.shareit.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import ru.practicum.shareit.errors.exceptions.RequestNotFoundException;
import ru.practicum.shareit.errors.exceptions.UserNotFoundException;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.RequestDtoItems;
import ru.practicum.shareit.request.dto.RequestMapper;
import ru.practicum.shareit.request.dto.RequestShortDto;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.request.repository.RequestRepository;
import ru.practicum.shareit.request.service.RequestServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RequestServiceTests {
    @Mock
    private UserRepository userRepository;
    @Mock
    private RequestRepository requestRepository;
    @Mock
    private ItemRepository itemRepository;
    @InjectMocks
    private RequestServiceImpl requestService;
    private RequestShortDto requestShortDto;
    private User user;
    private Request request;
    private Item item;

    @BeforeEach
    void beforeEach() {
        user = User.builder()
                .id(1L)
                .name("Борис")
                .email("boris@ya.ru")
                .build();

        request = Request.builder()
                .id(1L)
                .user(user)
                .description("Очень нужен велосипед")
                .build();

        item = Item.builder()
                .request(request)
                .owner(user)
                .build();

        requestShortDto = RequestShortDto.builder().description("Ищу велосипед в Мурино").build();
    }

    @Test
    void ifTryToCreateByUserWhoDoesNotExistsThenUserNotFoundException() {
        Mockito.when(userRepository.findById(1L))
                .thenThrow(new UserNotFoundException("Пользователь с таким id не найден"));

        final UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> requestService.create(1L, requestShortDto));

        assertEquals("Пользователь с таким id не найден", exception.getMessage());

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verify(requestRepository, Mockito.never())
                .save(Mockito.any(Request.class));
    }

    @Test
    void ifCreateThenCallRepositoryAndReturnDto() {
        request.setCreationTime(LocalDateTime.now());
        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        Mockito.when(requestRepository.save(Mockito.any(Request.class)))
                .thenReturn(request);

        ItemRequestDto item = requestService.create(1L, requestShortDto);
        assertThat(item, equalTo(RequestMapper.toRequestDto(request)));
        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verify(requestRepository, Mockito.times(1))
                .save(Mockito.any(Request.class));
    }

    @Test
    void ifThreeRequestsByUserFoundCallFindItemsItemRepository() {
        Request requestOne = Request.builder().id(1L).build();
        Request requestTwo = Request.builder().id(2L).build();
        Request requestThree = Request.builder().id(3L).build();
        Mockito.when(userRepository.existsById(1L))
                .thenReturn(true);
        Mockito.when(requestRepository.findAllByUserIdOrderByCreationTimeDesc(Mockito.anyLong()))
                .thenReturn(List.of(requestOne, requestTwo, requestThree));
        Mockito.when(itemRepository.findItemsByRequestId(Mockito.anyLong()))
                .thenReturn(List.of(item));

        requestService.findByUserId(1L);
        Mockito.verify(userRepository, Mockito.times(1))
                .existsById(1L);
        Mockito.verify(requestRepository, Mockito.times(1))
                .findAllByUserIdOrderByCreationTimeDesc(Mockito.anyLong());
        Mockito.verify(itemRepository, Mockito.times(3))
                .findItemsByRequestId(Mockito.anyLong());
    }

    @Test
    void ifRequestCalledByUserWhoDoesNotExistsThenUserNotFoundException() {
        Mockito.when(userRepository.existsById(1L))
                .thenReturn(false);

        final UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> requestService.findByUserId(1L));

        assertEquals("Пользователя с таким id не существует", exception.getMessage());
        Mockito.verify(userRepository, Mockito.times(1))
                .existsById(1L);
        Mockito.verify(requestRepository, Mockito.never())
                .findAllByUserIdOrderByCreationTimeDesc(Mockito.anyLong());
        Mockito.verify(itemRepository, Mockito.never())
                .findItemsByRequestId(Mockito.anyLong());
    }

    @Test
    void ifUserFoundThenReturnRequestsAndItems() {
        Mockito.when(userRepository.existsById(1L))
                .thenReturn(true);
        Mockito.when(requestRepository.findAllByUserIdOrderByCreationTimeDesc(Mockito.anyLong()))
                .thenReturn(List.of(request));
        Mockito.when(itemRepository.findItemsByRequestId(Mockito.anyLong()))
                .thenReturn(List.of(item));

        List<RequestDtoItems> itemsList = requestService.findByUserId(1L);
        assertNotNull(itemsList.get(0).getItems());
        Mockito.verify(userRepository, Mockito.times(1))
                .existsById(1L);
        Mockito.verify(requestRepository, Mockito.times(1))
                .findAllByUserIdOrderByCreationTimeDesc(Mockito.anyLong());
        Mockito.verify(itemRepository, Mockito.times(1))
                .findItemsByRequestId(Mockito.anyLong());
    }

    @Test
    void ifAllOtherUserRequestsFoundThenReturnRequestsWithItems() {
        Slice<Request> requests = new SliceImpl<>(List.of(request));

        Mockito.when(requestRepository.findAllOtherUsersRequests(Mockito.anyLong(), Mockito.any(Pageable.class)))
                .thenReturn(requests);
        Mockito.when(itemRepository.findItemsByRequestId(Mockito.anyLong()))
                .thenReturn(List.of(item));

        List<RequestDtoItems> requestList = requestService.findAllUsersRequests(1L, 0, 10);
        assertNotNull(requestList.get(0).getItems());
        assertThat(requestList.get(0).getItems(), equalTo(List.of(ItemMapper.toItemDto(item))));
        Mockito.verify(requestRepository, Mockito.times(1))
                .findAllOtherUsersRequests(Mockito.anyLong(), Mockito.any(Pageable.class));
        Mockito.verify(itemRepository, Mockito.times(1))
                .findItemsByRequestId(Mockito.anyLong());
    }

    @Test
    void ifTwoUserRequestsWereFoundThenCallFindItemsItemRepositoryTwice() {
        Request requestOne = Request.builder().id(1L).build();
        Request requestTwo = Request.builder().id(2L).build();
        Slice<Request> requests = new SliceImpl<>(List.of(requestOne, requestTwo));
        Mockito.when(requestRepository.findAllOtherUsersRequests(Mockito.anyLong(), Mockito.any(Pageable.class)))
                .thenReturn(requests);
        Mockito.when(itemRepository.findItemsByRequestId(Mockito.anyLong()))
                .thenReturn(List.of(item));

        requestService.findAllUsersRequests(1L, 0, 10);
        Mockito.verify(requestRepository, Mockito.times(1))
                .findAllOtherUsersRequests(Mockito.anyLong(), Mockito.any(Pageable.class));
        Mockito.verify(itemRepository, Mockito.times(2))
                .findItemsByRequestId(Mockito.anyLong());
    }

    @Test
    void ifTryToFindByNotExistingUserThenUserNotFoundException() {
        Mockito.when(userRepository.existsById(1L))
                .thenReturn(false);

        final UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> requestService.findById(1L, 5L));

        assertEquals("Пользователя с таким id не существует", exception.getMessage());
        Mockito.verify(userRepository, Mockito.times(1))
                .existsById(1L);
        Mockito.verify(requestRepository, Mockito.never())
                .findById(Mockito.anyLong());
        Mockito.verify(itemRepository, Mockito.never())
                .findItemsByRequestId(Mockito.anyLong());
    }

    @Test
    void ifRequestDoesNotExistThenRequestNotFoundException() {
        Mockito.when(userRepository.existsById(1L))
                .thenReturn(true);
        Mockito.when(requestRepository.findById(5L))
                .thenThrow(new RequestNotFoundException("Такого запроса не существует"));

        final RequestNotFoundException exception = assertThrows(
                RequestNotFoundException.class,
                () -> requestService.findById(1L, 5L));

        assertEquals("Такого запроса не существует", exception.getMessage());
        Mockito.verify(userRepository, Mockito.times(1))
                .existsById(1L);
        Mockito.verify(requestRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
        Mockito.verify(itemRepository, Mockito.never())
                .findItemsByRequestId(Mockito.anyLong());
    }

    @Test
    void ifRequestFoundByIdThenReturnRequestWithItems() {
        Mockito.when(userRepository.existsById(1L))
                .thenReturn(true);
        Mockito.when(requestRepository.findById(1L))
                .thenReturn(Optional.of(request));
        Mockito.when(itemRepository.findItemsByRequestId(1L))
                .thenReturn(List.of(item));

        RequestDtoItems items = requestService.findById(1L, 1L);
        assertNotNull(items.getItems());
        assertThat(items.getItems(), equalTo(List.of(ItemMapper.toItemDto(item))));
        Mockito.verify(userRepository, Mockito.times(1))
                .existsById(1L);
        Mockito.verify(requestRepository, Mockito.times(1))
                .findById(Mockito.anyLong());
        Mockito.verify(itemRepository, Mockito.times(1))
                .findItemsByRequestId(Mockito.anyLong());
    }
}