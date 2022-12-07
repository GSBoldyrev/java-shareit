package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.misc.Marker;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.Valid;

@Controller
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserController {

    private final UserClient userClient;

    @GetMapping("/{userId}")
    public ResponseEntity<Object> getById(@PathVariable long userId) {
        log.debug("Принят запрос на отображение пользователя {}", userId);

        return userClient.getUser(userId);
    }

    @GetMapping
    public ResponseEntity<Object> getUsers() {
        log.debug("Принят запрос на вывод всех пользователей");

        return userClient.getAll();
    }

    @PostMapping
    @Validated({Marker.OnCreate.class})
    public ResponseEntity<Object> createUser(@RequestBody @Valid UserDto userDto) {
        log.debug("Принят запрос на создание пользователя по имени {}", userDto.getName());

        return userClient.add(userDto);
    }

    @PatchMapping("/{userId}")
    @Validated({Marker.OnUpdate.class})
    public ResponseEntity<Object> updateUser(@PathVariable long userId,
                                             @RequestBody @Valid UserDto userDto) {
        log.debug("Принят запрос на обновление пользователя {}", userId);

        return userClient.update(userId, userDto);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Object> deleteUser(@PathVariable long userId) {
        log.debug("Принят запрос на удаление пользователя {}", userId);

        return userClient.delete(userId);
    }
}