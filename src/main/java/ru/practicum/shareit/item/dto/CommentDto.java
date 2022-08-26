package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CommentDto {
    private Long id;

    @NotNull(message = "Комментарий не может быть null")
    @NotBlank(message = "Комментарий не может быть пустым")
    private String text;

    private String authorName;
    private LocalDateTime created;
}
