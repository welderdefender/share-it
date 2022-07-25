package ru.practicum.shareit.errors.exceptions;

public class ValidationException extends RuntimeException {
    public ValidationException(String s) {
        super(s);
    }
}
