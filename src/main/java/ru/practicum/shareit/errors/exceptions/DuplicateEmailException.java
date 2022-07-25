package ru.practicum.shareit.errors.exceptions;

public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException(String s) {
        super(s);
    }

}
