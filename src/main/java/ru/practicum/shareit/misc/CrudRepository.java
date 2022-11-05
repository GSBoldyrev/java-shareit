package ru.practicum.shareit.misc;

import java.util.List;
import java.util.Optional;

public interface CrudRepository<T> {

    T add(T t);

    T update(T t);

    void delete(long id);

    Optional<T> findById(long id);

    List<T> findAll();
}
