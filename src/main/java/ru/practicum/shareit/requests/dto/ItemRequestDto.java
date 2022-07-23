package ru.practicum.shareit.requests.dto;

import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class ItemRequestDto {
    private Long id;
    private String description;
    private Long requester;
    private LocalDateTime created;
}