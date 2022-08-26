package ru.practicum.shareit.item.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import ru.practicum.shareit.item.model.Item;

@EnableJpaRepositories
public interface ItemRepository extends JpaRepository<Item, Long> {
    @Query(value = "select i from Item i where (upper(i.name) like upper(concat('%', ?1, '%')) " +
            "or upper(i.description) like upper(concat('%', ?1, '%'))) and i.available = true")
    List<Item> searchText(String text);

    List<Item> findItemsByOwnerId(long ownerId);

    boolean existsByOwnerId(long ownerId);
}
