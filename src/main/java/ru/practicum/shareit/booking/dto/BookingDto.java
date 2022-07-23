package ru.practicum.shareit.booking.dto;

import java.time.LocalDate;

import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.model.Status;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class BookingDto {
    private Long id;
    private LocalDate start;
    private LocalDate end;
    private Long item;
    private Long booker;
    private Status status;
}