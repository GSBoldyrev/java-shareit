package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.user.UserMapper.toUser;
import static ru.practicum.shareit.user.UserMapper.toUserDto;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;

    public UserDto add(UserDto userDto) {
        User userToAdd = toUser(userDto);

        return toUserDto(repository.save(userToAdd));
    }

    public UserDto update(UserDto userDto, long id) {
        User userToUpdate = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь по ID " + id + " не найден!"));
        User user = toUser(userDto);
        if (user.getName() != null) {
            userToUpdate.setName(user.getName());
        }
        if (user.getEmail() != null) {
            userToUpdate.setEmail(user.getEmail());
        }

        return toUserDto(repository.save(userToUpdate));
    }

    public void delete(long id) {
        repository.deleteById(id);
    }

    public UserDto getById(long id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь по ID " + id + " не найден!"));

        return toUserDto(user);
    }

    public List<UserDto> getAll() {
        return repository.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }
}
