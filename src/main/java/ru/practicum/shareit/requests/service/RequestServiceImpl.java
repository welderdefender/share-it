package ru.practicum.shareit.requests.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.errors.exceptions.RequestNotFoundException;
import ru.practicum.shareit.errors.exceptions.UserNotFoundException;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.pagination.Pagination;
import ru.practicum.shareit.requests.dto.ItemRequestDto;
import ru.practicum.shareit.requests.dto.RequestDtoItems;
import ru.practicum.shareit.requests.dto.RequestMapper;
import ru.practicum.shareit.requests.dto.RequestShortDto;
import ru.practicum.shareit.requests.model.Request;
import ru.practicum.shareit.requests.repository.RequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    public RequestServiceImpl(RequestRepository requestRepository, UserRepository userRepository,
                              ItemRepository itemRepository) {
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
    }

    @Override
    public ItemRequestDto create(long userId, RequestShortDto requestShortDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователя с таким id не существует"));

        return RequestMapper.toRequestDto(requestRepository.save(RequestMapper.toRequest(requestShortDto,
                LocalDateTime.now(), user)));
    }

    @Override
    public List<RequestDtoItems> findByUserId(long userId) {
        checkUser(userId);

        List<Request> requests = requestRepository.findAllByUserIdOrderByCreationTimeDesc(userId);

        return requests.stream()
                .map(request -> RequestMapper.toRequestDtoItems(request,
                        itemRepository.findItemsByRequestId(request.getId())))
                .collect(Collectors.toList());
    }

    @Override
    public List<RequestDtoItems> findAllUsersRequests(long userId, int from, int size) {
        Pageable sortedByDateDesc = Pagination.of(from, size, Sort.by("creationTime").descending());

        return requestRepository.findAllOtherUsersRequests(userId, sortedByDateDesc).get()
                .map(request -> RequestMapper.toRequestDtoItems(request,
                        itemRepository.findItemsByRequestId(request.getId())))
                .collect(Collectors.toList());
    }

    @Override
    public RequestDtoItems findById(long userId, long requestId) {
        checkUser(userId);
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RequestNotFoundException("Запроса с таким id не найдено"));

        return RequestMapper.toRequestDtoItems(request, itemRepository.findItemsByRequestId(requestId));
    }

    private void checkUser(long userId) {
        if (!userRepository.existsById(userId))
            throw new UserNotFoundException("Пользователя с таким id не существует");
    }
}
