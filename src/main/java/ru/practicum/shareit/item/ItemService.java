package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.error.exception.ConflictException;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.practicum.shareit.item.ItemMapper.toItem;
import static ru.practicum.shareit.item.ItemMapper.toItemDto;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository repository;
    private final UserService userService;

    public ItemDto getById(long itemId) {
        Item item = repository.findById(itemId).
                orElseThrow(() -> new NotFoundException("Вещь " + itemId + " не найдена!"));

        return toItemDto(item);
    }

    public List<ItemDto> getAll(long userId) {
        return repository.findAll().stream().
                filter(item -> item.getOwnerId() == userId).
                map(ItemMapper::toItemDto).
                collect(Collectors.toList());
    }

    public ItemDto add(ItemDto itemDto, long userId) {
        userService.getById(userId);
        Item item = toItem(itemDto);
        item.setOwnerId(userId);

        return toItemDto(repository.add(item));
    }

    public ItemDto update(ItemDto itemDto, long userId) {
        userService.getById(userId);
        Item item = toItem(itemDto);
        long id = item.getId();
        Optional<Item> optionalItem = repository.findById(id).
                filter(i -> i.getOwnerId() == userId);
        Item itemToUpdate = optionalItem.
                orElseThrow(() -> new NotFoundException("Вещь  " + id + " не найдена!"));
        if (item.getName() != null) {
            itemToUpdate.setName(item.getName());
        }
        if (item.getDescription() != null) {
            itemToUpdate.setDescription(item.getDescription());
        }
        if (item.getAvailable() != null) {
            itemToUpdate.setAvailable(item.getAvailable());
        }

        return toItemDto(repository.update(itemToUpdate));
    }

    public void delete(long itemId, long userId) {
        Optional<Item> optionalItem = repository.findById(itemId).
                filter(i -> i.getOwnerId() == userId);
        if (optionalItem.isPresent()) {
            repository.delete(itemId);
        } else {
            throw new ConflictException("Это ведь не ваша вещь, чтоб ее удалять!");
        }
    }

    public List<ItemDto> search(String text) {
        if (text.isBlank()) {
            return new ArrayList<>();
        }
        String query = text.toLowerCase();
        List<Item> items = repository.search(query);
        if (items.isEmpty()) {
            throw new NotFoundException("Искомая вещь не найдена!");
        }

        return items.stream().map(ItemMapper::toItemDto).collect(Collectors.toList());
    }
}
