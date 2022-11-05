package ru.practicum.shareit.item.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.*;

@Repository
@Slf4j
public class InMemoryItemRepository implements ItemRepository {

    private final Map<Long, Item> items = new HashMap<>();
    private long id;

    @Override
    public Item add(Item item) {
        item.setId(generateId());
        items.put(item.getId(), item);
        log.info("Вещь {} успешно добавлена!", item.getName());

        return item;
    }

    @Override
    public Item update(Item item) {
        items.put(item.getId(), item);
        log.info("Вещь {} успешно обновлена!", item.getName());

        return item;
    }

    @Override
    public Optional<Item> findById(long itemId) {
        return Optional.ofNullable(items.get(itemId));
    }

    @Override
    public List<Item> findAll() {
        return new ArrayList<>(items.values());
    }

    @Override
    public void delete(long itemId) {
        Item item = items.remove(itemId);
        log.info("Вещь {} успешно удалена!", item.getName());

    }

    @Override
    public List<Item> search(String query) {
        List<Item> result = new ArrayList<>();
        for (Item i : items.values()) {
            String name = i.getName().toLowerCase();
            String description = i.getDescription().toLowerCase();
            if ((name.contains(query) || description.contains(query)) && i.getAvailable()) {
                result.add(i);
            }
        }

        return result;
    }

    private long generateId() {
        return ++id;
    }
}
