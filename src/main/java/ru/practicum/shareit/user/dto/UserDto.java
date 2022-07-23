package ru.practicum.shareit.user.dto;

import javax.validation.constraints.Email;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.Data;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private Long id;
    private String name;
    @Email
    private String email;
}
