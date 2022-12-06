package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoFull;

import javax.validation.constraints.Min;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
@Slf4j
public class ItemRequestController {

    private final ItemRequestService service;

    @PostMapping
    public ItemRequestDto addRequest(@RequestHeader("X-Sharer-User-Id") long userId,
                                     @RequestBody ItemRequestDto requestDto) {
        log.debug("Создание нового запроса от пользователя {}", userId);
        return service.add(requestDto, userId);
    }

    @GetMapping
    public List<ItemRequestDtoFull> getForAuthor(@RequestHeader("X-Sharer-User-Id") long userId) {
        log.debug("Просмотр всех запросов от пользователя {}", userId);
        return service.getForAuthor(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDtoFull> getAll(@RequestHeader("X-Sharer-User-Id") long userId,
                                           @RequestParam(defaultValue = "0") @Min(0) int from,
                                           @RequestParam(defaultValue = "100") @Min(1) int size) {
        log.debug("Вывод всех запросов начиная с {}, по {} запросов на странице", from, size);
        return service.getAll(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDtoFull getById(@RequestHeader("X-Sharer-User-Id") long userId,
                                  @PathVariable long requestId) {
        log.debug("Вывод запроса {}", requestId);
        return service.getById(userId, requestId);
    }
}
