package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoShort;
import ru.practicum.shareit.misc.Marker;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

@RestController
@Validated
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService service;

    @GetMapping
    public List<ItemDto> getItems(@RequestHeader("X-Sharer-User-Id") long userId,
                                  @RequestParam(defaultValue = "0") @Min(0) int from,
                                  @RequestParam(defaultValue = "100") @Min(1) int size) {
        return service.getAll(userId, from, size);
    }

    @GetMapping("/{itemId}")
    public ItemDto getById(@RequestHeader("X-Sharer-User-Id") long userId, @PathVariable long itemId) {
        return service.getById(itemId, userId);
    }

    @GetMapping("/search")
    public List<ItemDtoShort> searchItem(@RequestParam String text,
                                         @RequestParam(defaultValue = "0") @Min(0) int from,
                                         @RequestParam(defaultValue = "100") @Min(1) int size) {
        return service.search(text, from, size);
    }

    @PostMapping
    @Validated({Marker.OnCreate.class})
    public ItemDtoShort addItem(@RequestHeader("X-Sharer-User-Id") long userId,
                                @RequestBody @Valid ItemDto itemDto) {
        return service.add(itemDto, userId);
    }

    @PatchMapping("/{itemId}")
    @Validated({Marker.OnUpdate.class})
    public ItemDtoShort updateItem(@RequestHeader("X-Sharer-User-Id") long userId,
                                   @PathVariable long itemId,
                                   @RequestBody @Valid ItemDto itemDto) {
        itemDto.setId(itemId);

        return service.update(itemDto, userId);
    }

    @DeleteMapping("/{itemId}")
    public void deleteItem(@RequestHeader("X-Sharer-User-Id") long userId,
                           @PathVariable long itemId) {
        service.delete(itemId, userId);
    }

    @PostMapping("/{itemId}/comment")
    @Validated
    public CommentDto addComment(@RequestHeader("X-Sharer-User-Id") long userId,
                                 @PathVariable long itemId,
                                 @RequestBody @Valid CommentDto commentDto) {
        return service.addComment(commentDto, itemId, userId);
    }
}
