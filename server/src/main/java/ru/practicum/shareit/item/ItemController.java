package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoShort;

import javax.validation.constraints.Min;
import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
public class ItemController {

    private final ItemService service;

    @GetMapping
    public List<ItemDto> getItems(@RequestHeader("X-Sharer-User-Id") long userId,
                                  @RequestParam(defaultValue = "0") @Min(0) int from,
                                  @RequestParam(defaultValue = "100") @Min(1) int size) {
        log.debug("Запрос на вывод всех вещей, начиная с {}, по {} на страницу", from, size);
        return service.getAll(userId, from, size);
    }

    @GetMapping("/{itemId}")
    public ItemDto getById(@RequestHeader("X-Sharer-User-Id") long userId, @PathVariable long itemId) {
        log.debug("Запрос на вывод вещи {}", itemId);
        return service.getById(itemId, userId);
    }

    @GetMapping("/search")
    public List<ItemDtoShort> searchItem(@RequestParam String text,
                                         @RequestParam(defaultValue = "0") @Min(0) int from,
                                         @RequestParam(defaultValue = "100") @Min(1) int size) {
        log.debug("Запрос на поиск вещей, содержащих в названии или описании {}", text);
        return service.search(text, from, size);
    }

    @PostMapping
    public ItemDtoShort addItem(@RequestHeader("X-Sharer-User-Id") long userId,
                                @RequestBody ItemDto itemDto) {
        log.debug("Запрос на добавление новой вещи {}", itemDto.getName());
        return service.add(itemDto, userId);
    }

    @PatchMapping("/{itemId}")
    public ItemDtoShort updateItem(@RequestHeader("X-Sharer-User-Id") long userId,
                                   @PathVariable long itemId,
                                   @RequestBody ItemDto itemDto) {
        log.debug("Запрос на обновление вещи {}", itemId);
        itemDto.setId(itemId);

        return service.update(itemDto, userId);
    }

    @DeleteMapping("/{itemId}")
    public void deleteItem(@RequestHeader("X-Sharer-User-Id") long userId,
                           @PathVariable long itemId) {
        log.debug("Запрос на удаление вещи {}", itemId);
        service.delete(itemId, userId);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@RequestHeader("X-Sharer-User-Id") long userId,
                                 @PathVariable long itemId,
                                 @RequestBody CommentDto commentDto) {
        log.debug("запрос на добавление комментария к вещи {} от пользователя {}", itemId, userId);
        return service.addComment(commentDto, itemId, userId);
    }
}
