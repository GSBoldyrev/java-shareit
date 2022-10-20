package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static ru.practicum.shareit.user.UserMapper.toUser;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    @GetMapping("/{userId}")
    public User getById(@PathVariable long userId) {

        return service.getById(userId);
    }

    @GetMapping
    public List<User> getUsers() {

        return service.getAll();
    }

    @PostMapping
    public User createUser(@RequestBody UserDto userDto) {

        return service.add(toUser(userDto));
    }

    @PatchMapping("/{userId}")
    public User updateUser(@PathVariable long userId, @RequestBody UserDto userDto) {
        User user = toUser(userDto);
        user.setId(userId);

        return service.update(user);
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable long userId) {
        service.delete(userId);
    }
}
