package ru.practicum.shareit.item.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Item;

@EnableJpaRepositories
public interface ItemRepository extends JpaRepository<Item, Long> {
    @Query(value = "select i from Item i where (upper(i.name) like upper(concat('%', :text, '%')) or upper(i" +
            ".description) like upper(concat('%', :text, '%'))) and i.available = true")
    Slice<Item> searchText(@Param("text") String text, Pageable pageable);

    Slice<Item> findItemsByOwnerId(long ownerId, Pageable pageable);

    List<Item> findItemsByRequestId(long requestId);

    boolean existsByOwnerId(long ownerId);
}
