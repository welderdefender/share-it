package ru.practicum.shareit.services.implementations;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.RequestDtoItems;
import ru.practicum.shareit.request.dto.RequestShortDto;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.request.service.RequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.service.UserService;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class RequestServiceImplTests {
    private final EntityManager tem;
    private final UserService userService;
    private final RequestService requestService;
    private final ItemService itemService;
    private static UserDto userDto;
    private static RequestShortDto requestShortDto;

    @BeforeAll
    public static void beforeAll() {
        userDto = UserDto.builder()
                .email("test@ya.ru")
                .name("Тестировщик")
                .build();

        requestShortDto = RequestShortDto.builder()
                .description("Ищу велосипед")
                .build();
    }

    @Test
    void createRequest() {
        UserDto userToSave = userService.create(userDto);
        ItemRequestDto requestToSave = requestService.create(userToSave.getId(), requestShortDto);
        TypedQuery<Request> query = tem.createQuery("Select r from Request r where r.id = :requestId", Request.class);
        Request request = query
                .setParameter("requestId", requestToSave.getId())
                .getSingleResult();

        assertThat(requestToSave.getId(), notNullValue());
        assertThat(requestToSave.getCreated(), notNullValue());
        assertThat(requestToSave.getDescription(), equalTo(request.getDescription()));
        assertThat(requestToSave.getUserId(), equalTo(userToSave.getId()));
    }

    @Test
    void findById() {
        UserDto userToSave = userService.create(userDto);
        ItemRequestDto savedRequest = requestService.create(userToSave.getId(), requestShortDto);
        TypedQuery<Request> query = tem.createQuery("Select r from Request r where r.id = :requestId", Request.class);

        Request request = query
                .setParameter("requestId", savedRequest.getId())
                .getSingleResult();
        assertThat(savedRequest.getId(), equalTo(request.getId()));
        assertThat(savedRequest.getCreated(), equalTo(request.getCreationTime()));
        assertThat(savedRequest.getDescription(), equalTo(request.getDescription()));
        assertThat(savedRequest.getUserId(), equalTo(request.getUser().getId()));
    }

    @Test
    void findRequestsByUser() {
        UserDto userToSave = userService.create(userDto);
        ItemRequestDto savedRequest = requestService.create(userToSave.getId(), requestShortDto);
        RequestShortDto requestTwo = RequestShortDto.builder()
                .description("Ищу зеленый велосипед")
                .build();
        ItemRequestDto savedSecondRequest = requestService.create(userToSave.getId(), requestTwo);

        UserDto newUser = UserDto.builder()
                .name("Даниил")
                .email("daniil@ya.ru")
                .build();
        RequestShortDto anotherRequest = RequestShortDto.builder()
                .description("Ищу велосипед")
                .build();
        UserDto returnedNewUser = userService.create(newUser);
        requestService.create(returnedNewUser.getId(), anotherRequest);

        List<RequestDtoItems> userRequests = requestService.findByUserId(userToSave.getId());

        TypedQuery<Request> query = tem.createQuery("Select r from Request r where r.user.id = :userId", Request.class);
        List<Request> requestsList = query
                .setParameter("userId", userToSave.getId())
                .getResultList();

        assertThat(requestsList.size(), equalTo(2));
        assertThat(requestsList.get(0).getId(), equalTo(requestsList.get(0).getId()));
        assertThat(requestsList.get(0).getUser(), equalTo(UserMapper.toUser(userToSave)));
        assertThat(requestsList.get(0).getDescription(), equalTo(savedRequest.getDescription()));
        assertThat(requestsList.get(1).getId(), equalTo(requestsList.get(1).getId()));
        assertThat(requestsList.get(1).getUser(), equalTo(UserMapper.toUser(userToSave)));
        assertThat(requestsList.get(1).getDescription(), equalTo(savedSecondRequest.getDescription()));
    }

    @Test
    void findAllOtherUsersRequests() {
        UserDto userToSave = userService.create(userDto);
        ItemRequestDto firstRequestToSave = requestService.create(userToSave.getId(), requestShortDto);
        RequestShortDto request = RequestShortDto.builder()
                .description("Ищу зеленый велосипед")
                .build();

        ItemRequestDto secondRequestToSave = requestService.create(userToSave.getId(), request);

        UserDto newUser = UserDto.builder()
                .name("another")
                .email("another@gmail.com")
                .build();
        RequestShortDto anotherRequest = RequestShortDto.builder()
                .description("I need something")
                .build();

        UserDto returnedNewUser = userService.create(newUser);
        requestService.create(returnedNewUser.getId(), anotherRequest);
        List<RequestDtoItems> newUserRequests = requestService.findAllUsersRequests(returnedNewUser.getId(), 0, 10);
        TypedQuery<Request> query = tem.createQuery("Select r from Request r where r.user.id <> :userId", Request.class);
        List<Request> requests = query
                .setParameter("userId", returnedNewUser.getId())
                .getResultList();

        assertThat(requests.size(), equalTo(2));
        assertThat(requests.get(0).getId(), equalTo(requests.get(0).getId()));
        assertThat(requests.get(0).getUser(), equalTo(UserMapper.toUser(userToSave)));
        assertThat(requests.get(0).getDescription(), equalTo(firstRequestToSave.getDescription()));
        assertThat(requests.get(1).getId(), equalTo(requests.get(1).getId()));
        assertThat(requests.get(1).getUser(), equalTo(UserMapper.toUser(userToSave)));
        assertThat(requests.get(1).getDescription(), equalTo(secondRequestToSave.getDescription()));
    }
}
