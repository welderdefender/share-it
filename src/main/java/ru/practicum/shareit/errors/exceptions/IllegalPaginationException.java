package ru.practicum.shareit.errors.exceptions;

public class IllegalPaginationException extends RuntimeException {
    public IllegalPaginationException(String message) {
        super(message);
    }
}