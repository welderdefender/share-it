package ru.practicum.shareit.booking.dto;

import java.time.LocalDateTime;

import lombok.*;

import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Data
@AllArgsConstructor
@Builder
public class BookingDto {
    private Long id;
    private Long bookerId;
    private LocalDateTime start;
    private LocalDateTime end;
}