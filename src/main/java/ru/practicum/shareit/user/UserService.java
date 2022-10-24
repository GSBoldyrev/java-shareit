package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.error.exception.ConflictException;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.misc.CrudRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.practicum.shareit.user.UserMapper.toUser;
import static ru.practicum.shareit.user.UserMapper.toUserDto;

@Service
@RequiredArgsConstructor
public class UserService {

    private final CrudRepository<User> repository;

    public UserDto add(UserDto userDto) {
        User userToAdd = toUser(userDto);
        checkEmail(userToAdd);

        return toUserDto(repository.add(userToAdd));
    }

    public UserDto update(UserDto userDto, long id) {
        Optional<User> optionalUser = repository.findById(id);
        User userToUpdate = optionalUser.
                orElseThrow(() -> new NotFoundException("Пользователь по ID " + id + " не найден!"));
        User user = toUser(userDto);
        if (user.getName() != null) {
            userToUpdate.setName(user.getName());
        }
        if (user.getEmail() != null) {
            checkEmail(user);
            userToUpdate.setEmail(user.getEmail());
        }

        return toUserDto(repository.update(userToUpdate));
    }

    public void delete(long id) {
        repository.delete(id);
    }

    public UserDto getById(long id) {
        Optional<User> optionalUser = repository.findById(id);
        User user = optionalUser.
                orElseThrow(() -> new NotFoundException("Пользователь по ID " + id + " не найден!"));

        return toUserDto(user);
    }

    public List<UserDto> getAll() {
        return repository.findAll().stream().
                map(UserMapper::toUserDto).
                collect(Collectors.toList());
    }

    private void checkEmail(User user) {
        for (User u : repository.findAll()) {
            if (user.getEmail().equals(u.getEmail())) {
                throw new ConflictException("Такой адрес электронной почты уже существует!");
            }
        }
    }
}
