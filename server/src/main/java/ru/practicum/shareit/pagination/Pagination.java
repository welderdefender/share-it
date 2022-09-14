package ru.practicum.shareit.pagination;

import lombok.Data;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.errors.exceptions.IllegalPaginationException;

@Data
public class Pagination implements Pageable {
    private final int from;
    private final int size;
    private final Sort sort;

    private Pagination(int from, int size, Sort sort) {
        this.from = from;
        this.size = size;
        this.sort = sort;
    }

    private Pagination(int from, int size) {
        this.from = from;
        this.size = size;
        sort = Sort.unsorted();
    }

    @Override
    public int getPageNumber() {
        return 0;
    }

    @Override
    public int getPageSize() {
        return size;
    }

    @Override
    public long getOffset() {
        return from;
    }

    @Override
    public Sort getSort() {
        return sort;
    }

    @Override
    public Pageable next() {
        return new Pagination(from + size, size, sort);
    }

    @Override
    public Pageable previousOrFirst() {
        return new Pagination(from, size, sort);
    }

    @Override
    public Pageable first() {
        return new Pagination(from, size, sort);
    }

    @Override
    public Pageable withPage(int pageNumber) {
        return new Pagination(from + size * pageNumber, size, sort);
    }

    @Override
    public boolean hasPrevious() {
        return false;
    }

    public static Pageable of(int from, int size) {
        validateArguments(from, size);
        return new Pagination(from, size);
    }

    public static Pageable of(int from, int size, Sort sort) {
        validateArguments(from, size);
        return new Pagination(from, size, sort);
    }

    private static void validateArguments(int from, int size) {
        if (from < 0)
            throw new IllegalPaginationException("Переменная from должна быть больше, либо равна 0");
        if (size < 1)
            throw new IllegalPaginationException("переменная size должна быть больше, либо равна 1");
    }
}
