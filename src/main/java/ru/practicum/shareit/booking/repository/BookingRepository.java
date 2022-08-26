package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@EnableJpaRepositories
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> getTopByItem_IdAndBooker_IdOrderByEndAsc(long itemId, long bookerId);

    Optional<Booking> getTopByItem_IdAndEndBeforeOrderByStartDesc(long itemId, LocalDateTime localDateTime);

    Optional<Booking> getTopByItem_IdAndStartAfterOrderByStartDesc(long itemId, LocalDateTime localDateTime);

    List<Booking> getBookingsByBookerIdOrderByStartDesc(long bookerId);

    @Query(value = "select b from Booking b left join Item i on b.item.id = i.id where i.owner.id = ?1 order by b.start desc")
    List<Booking> findBookingsByOwnerId(long ownerId);
}
