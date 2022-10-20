package ru.practicum.shareit.user.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class InMemoryUserRepository implements UserRepository {

    private final Map<Long, User> users = new HashMap<>();
    private long id;

    @Override
    public User add(User user) {
        user.setId(generateId());
        users.put(user.getId(), user);

        return user;
    }

    @Override
    public User update(User user) {
        users.put(user.getId(), user);

        return user;
    }

    @Override
    public void delete(long id) {
        users.remove(id);
    }

    @Override
    public User get(long id) {
        return users.get(id);
    }

    @Override
    public List<User> getAll() {
        return users.values().stream().toList();
    }

    private long generateId() {
        return ++id;
    }

}
