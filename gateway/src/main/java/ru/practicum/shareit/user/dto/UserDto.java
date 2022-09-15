package ru.practicum.shareit.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UserDto {
    private String name;
    @NotNull(message = "Email не может быть null")
    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Неправильно введен Email")
    private String email;
}
