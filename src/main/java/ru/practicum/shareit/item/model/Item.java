package ru.practicum.shareit.item.model;

import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.requests.ItemRequest;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Item {
    private Long id;
    @NotNull
    @NotBlank
    private String name;
    private String description;
    private Boolean available;
    private User owner;
    private ItemRequest request;
}