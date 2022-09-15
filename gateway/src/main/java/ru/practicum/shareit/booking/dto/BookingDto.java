package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.Positive;
import java.time.LocalDateTime;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class BookingDto {
    @Positive
    private Long itemId;
    @Positive
    private Long bookerId;
    @DateTimeFormat(iso = DATE_TIME)
    @FutureOrPresent(message = "Время не может быть раньше текущего")
    private LocalDateTime start;
    @DateTimeFormat(iso = DATE_TIME)
    private LocalDateTime end;
}
