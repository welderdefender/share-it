package ru.practicum.shareit.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import ru.practicum.shareit.user.model.User;

@EnableJpaRepositories
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsUserByEmail(String email);
}
