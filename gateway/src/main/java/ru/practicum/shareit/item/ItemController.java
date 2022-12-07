package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.misc.Marker;

import javax.validation.Valid;
import javax.validation.constraints.Min;

@Controller
@RequestMapping(path = "/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {

    private final ItemClient itemClient;

    @GetMapping
    public ResponseEntity<Object> getItems(@RequestHeader("X-Sharer-User-Id") long userId,
                                           @RequestParam(defaultValue = "0") @Min(0) int from,
                                           @RequestParam(defaultValue = "100") @Min(1) int size) {
        log.debug("Запрос на вывод всех вещей, начиная с {}, по {} на страницу", from, size);

        return itemClient.getAll(userId, from, size);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getById(@RequestHeader("X-Sharer-User-Id") long userId,
                                          @PathVariable long itemId) {
        log.debug("Запрос на вывод вещи {}", itemId);

        return itemClient.getById(userId, itemId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItem(@RequestHeader("X-Sharer-User-Id") long userId,
                                             @RequestParam String text,
                                             @RequestParam(defaultValue = "0") @Min(0) int from,
                                             @RequestParam(defaultValue = "100") @Min(1) int size) {
        log.debug("Запрос на поиск вещей, содержащих в названии или описании {}", text);

        return itemClient.search(userId, text, from, size);
    }

    @PostMapping
    @Validated({Marker.OnCreate.class})
    public ResponseEntity<Object> addItem(@RequestHeader("X-Sharer-User-Id") long userId,
                                          @RequestBody @Valid ItemDto itemDto) {
        log.debug("Запрос на добавление новой вещи {}", itemDto.getName());

        return itemClient.add(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    @Validated({Marker.OnUpdate.class})
    public ResponseEntity<Object> updateItem(@RequestHeader("X-Sharer-User-Id") long userId,
                                             @PathVariable long itemId,
                                             @RequestBody @Valid ItemDto itemDto) {
        log.debug("Запрос на обновление вещи {}", itemId);
        itemDto.setId(itemId);

        return itemClient.update(userId, itemId, itemDto);
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Object> deleteItem(@RequestHeader("X-Sharer-User-Id") long userId,
                                             @PathVariable long itemId) {
        log.debug("Запрос на удаление вещи {}", itemId);

        return itemClient.delete(userId, itemId);
    }

    @PostMapping("/{itemId}/comment")
    @Validated
    public ResponseEntity<Object> addComment(@RequestHeader("X-Sharer-User-Id") long userId,
                                             @PathVariable long itemId,
                                             @RequestBody @Valid CommentDto commentDto) {
        log.debug("запрос на добавление комментария к вещи {} от пользователя {}", itemId, userId);

        return itemClient.addComment(userId, itemId, commentDto);
    }
}