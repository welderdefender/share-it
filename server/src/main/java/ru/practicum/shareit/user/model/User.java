package ru.practicum.shareit.user.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "user_name")
    private String name;

    @Email(message = "Некорректный Email")
    @NotNull(message = "Email не должен равняться null")
    @NotBlank(message = "Email не может быть пустым")
    @Column(name = "email", unique = true)
    private String email;

    @JsonProperty("id")
    public Long getId() {
        return id;
    }
}
