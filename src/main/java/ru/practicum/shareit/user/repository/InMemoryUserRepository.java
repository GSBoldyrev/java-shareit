package ru.practicum.shareit.user.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.misc.CrudRepository;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@Repository
@Slf4j
public class InMemoryUserRepository implements CrudRepository<User> {

    private final Map<Long, User> users = new HashMap<>();
    private long id;

    @Override
    public User add(User user) {
        user.setId(generateId());
        users.put(user.getId(), user);
        log.info("Пользователь {} успешно добавлен!", user.getName());

        return user;
    }

    @Override
    public User update(User user) {
        users.put(user.getId(), user);
        log.info("Пользователь {} успешно обновлен!", user.getName());

        return user;
    }

    @Override
    public void delete(long id) {
        User user = users.remove(id);
        log.info("Пользователь {} успешно удален!", user.getName());
    }

    @Override
    public Optional<User> findById(long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    private long generateId() {
        return ++id;
    }
}
