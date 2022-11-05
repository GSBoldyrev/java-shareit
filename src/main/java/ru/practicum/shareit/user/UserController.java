package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.misc.Marker;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.Valid;
import java.util.List;

@RestController
@Validated
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    @GetMapping("/{userId}")
    public UserDto getById(@PathVariable long userId) {
        return service.getById(userId);
    }

    @GetMapping
    public List<UserDto> getUsers() {
        return service.getAll();
    }

    @PostMapping
    @Validated({Marker.OnCreate.class})
    public UserDto createUser(@RequestBody @Valid UserDto userDto) {
        return service.add(userDto);
    }

    @PatchMapping("/{userId}")
    @Validated({Marker.OnUpdate.class})
    public UserDto updateUser(@PathVariable long userId, @RequestBody @Valid UserDto userDto) {
        return service.update(userDto, userId);
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable long userId) {
        service.delete(userId);
    }
}
