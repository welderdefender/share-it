package ru.practicum.shareit.item.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.requests.ItemRequest;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Entity
@Table(name = "item")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull(message = "Имя не может быть null")
    @NotBlank(message = "Имя не может быть пустым")
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull(message = "Описание не может быть null")
    @NotBlank(message = "Описание не может быть пустым")
    @Column(name = "description", nullable = false)
    private String description;

    @NotNull(message = "Доступность не может быть null")
    @Column(name = "available", nullable = false)
    private Boolean available;

    @ManyToOne
    @JoinColumn(name = "owner")
    private User owner;

    @ManyToOne
    @JoinColumn(name = "request")
    private ItemRequest itemRequest;

    @JsonProperty("id")
    public Long getId() {
        return id;
    }
}