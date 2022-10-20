package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.UserService;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Validated
public class ItemService {

    private final ItemRepository repository;
    private final UserService userService;

    public Item getById(long itemId, long userId) {
        checkOwner(userId);

        return repository.get(itemId);
    }

    public List<Item> getAll(long userId) {
        checkOwner(userId);
        List<Item> result = new ArrayList<>();
        for (Item i: repository.getAll()) {
            if (i.getOwnerId() == userId) {
                result.add(i);
            }
        }

        return result;
    }

    public Item add(@Valid Item item, long userId) {
        checkOwner(userId);

        return repository.add(item);
    }

    public Item update(Item item, long userId) {
        checkOwner(userId);
        Item itemToUpdate = repository.get(item.getId());
        if (itemToUpdate.getOwnerId() != userId) {
            throw new NotFoundException("У данного пользователя нет такой вещи!");
        }
        if (item.getName() != null) {
            itemToUpdate.setName(item.getName());
        }
        if (item.getDescription() != null) {
            itemToUpdate.setDescription(item.getDescription());
        }
        if (item.getAvailable() != null) {
            itemToUpdate.setAvailable(item.getAvailable());
        }

        return repository.update(itemToUpdate);
    }

    public void delete(long itemId, long userId) {
        checkOwner(userId);
        repository.delete(itemId);
    }

    public List<Item> search(String text, long userId) {
        checkOwner(userId);
        if (text.isBlank()) {
            return new ArrayList<>();
        }
        String query = text.toLowerCase();
        List<Item> result = repository.search(query);
        if (result == null) {
            throw new NotFoundException("Искомая вещь не найдена!");
        }

        return result;
    }

    private void checkOwner(long userId) {
        if (userService.getById(userId) == null) {
            throw new NotFoundException("Пользователь не найден!");
        }
    }
}
