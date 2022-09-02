package ru.practicum.shareit.requests.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.requests.model.Request;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findAllByUserIdOrderByCreationTimeDesc(long userId);

    @Query(value = "select r from Request r where r.user.id <> :userId")
    Slice<Request> findAllOtherUsersRequests(@Param("userId") long userId, Pageable pageable);
}
