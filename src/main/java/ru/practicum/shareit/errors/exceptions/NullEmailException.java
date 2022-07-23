package ru.practicum.shareit.errors.exceptions;

public class NullEmailException extends RuntimeException {
    public NullEmailException(String s) {
        super(s);
    }
}
