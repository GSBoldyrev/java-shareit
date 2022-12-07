package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.misc.Marker;
import ru.practicum.shareit.request.dto.RequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Min;

@Controller
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
@Validated
public class RequestController {

    private final RequestClient requestClient;

    @PostMapping
    @Validated({Marker.OnCreate.class})
    public ResponseEntity<Object> addRequest(@RequestHeader("X-Sharer-User-Id") long userId,
                                             @RequestBody @Valid RequestDto requestDto) {
        log.debug("Создание нового запроса от пользователя {}", userId);

        return requestClient.add(userId, requestDto);
    }

    @GetMapping
    public ResponseEntity<Object> getForAuthor(@RequestHeader("X-Sharer-User-Id") long userId) {
        log.debug("Просмотр всех запросов от пользователя {}", userId);

        return requestClient.getForAuthor(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAll(@RequestHeader("X-Sharer-User-Id") long userId,
                                         @RequestParam(defaultValue = "0") @Min(0) int from,
                                         @RequestParam(defaultValue = "100") @Min(1) int size) {
        log.debug("Вывод всех запросов начиная с {}, по {} запросов на странице", from, size);

        return requestClient.getAll(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getById(@RequestHeader("X-Sharer-User-Id") long userId,
                                          @PathVariable long requestId) {
        log.debug("Вывод запроса {}", requestId);

        return requestClient.getById(userId, requestId);
    }
}