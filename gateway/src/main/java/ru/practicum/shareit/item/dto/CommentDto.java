package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CommentDto {
    @NotNull(message = "Текст не может быть null")
    @NotBlank(message = "Текст не может быть пустым")
    private String text;
    private String authorName;
}
