package ru.practicum.shareit.booking.dto;

import java.time.LocalDateTime;

import lombok.*;

@Data
@AllArgsConstructor
@Builder
public class BookingDto {
    private Long id;
    private Long bookerId;
    private LocalDateTime start;
    private LocalDateTime end;
}