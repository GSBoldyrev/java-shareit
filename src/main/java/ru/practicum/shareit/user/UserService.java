package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.practicum.shareit.error.exception.ConflictException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.validation.Valid;
import java.util.List;

@Service
@RequiredArgsConstructor
@Validated
public class UserService {

    private final UserRepository repository;

    public User add(@Valid User user) {
        checkEmail(user);

        return repository.add(user);
    }

    public User update(User user) {
        User userToUpdate = repository.get(user.getId());
        if (user.getName() != null) {
            userToUpdate.setName(user.getName());
        }
        if (user.getEmail() != null) {
            checkEmail(user);
            userToUpdate.setEmail(user.getEmail());
        }
        return repository.update(userToUpdate);
    }

    public void delete(long id) {
        repository.delete(id);
    }

    public User getById(long id) {

        return repository.get(id);
    }

    public List<User> getAll() {

        return repository.getAll();
    }

    private void checkEmail(User user) {
        for (User u : repository.getAll()) {
            if (user.getEmail().equals(u.getEmail())) {
                throw new ConflictException("Такой адрес электронной почты уже существует!");
            }
        }
    }

}
