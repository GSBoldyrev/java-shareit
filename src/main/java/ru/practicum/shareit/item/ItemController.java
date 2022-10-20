package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.List;

import static ru.practicum.shareit.item.ItemMapper.toItem;
import static ru.practicum.shareit.item.ItemMapper.toItemDto;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService service;

    @GetMapping
    public List<ItemDto> getItems(@RequestHeader("X-Sharer-User-Id") long userId) {
        List<Item> items = service.getAll(userId);
        List<ItemDto> result = new ArrayList<>();
        for (Item i: items) {
            result.add(toItemDto(i));
        }

        return result;
    }

    @GetMapping("/{itemId}")
    public ItemDto getById(@RequestHeader("X-Sharer-User-Id") long userId,
                           @PathVariable long itemId) {


        return toItemDto(service.getById(itemId, userId));
    }

    @GetMapping("/search")
    public List<ItemDto> searchItem(@RequestHeader("X-Sharer-User-Id") long userId,
                              @RequestParam String text) {
        List<Item> items = service.search(text, userId);
        List<ItemDto> result = new ArrayList<>();
        for (Item i: items) {
            result.add(toItemDto(i));
        }

        return result;
    }

    @PostMapping
    public ItemDto addItem(@RequestHeader("X-Sharer-User-Id") long userId,
                           @RequestBody ItemDto itemDto) {
        Item item = toItem(itemDto);
        item.setOwnerId(userId);

        return toItemDto(service.add(item, userId));
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestHeader("X-Sharer-User-Id") long userId,
                              @PathVariable long itemId,
                              @RequestBody ItemDto itemDto) {
        Item item = toItem(itemDto);
        item.setId(itemId);

        return toItemDto(service.update(item, userId));
    }

    @DeleteMapping("/{itemId}")
    public void deleteItem(@RequestHeader("X-Sharer-User-Id") long userId,
                           @PathVariable long itemId) {
        service.delete(itemId, userId);
    }
}
