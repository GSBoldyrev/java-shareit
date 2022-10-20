package ru.practicum.shareit.item.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class InMemoryItemRepository implements ItemRepository {

    private final Map<Long, Item> items = new HashMap<>();
    private long id;

    @Override
    public Item add(Item item) {
        item.setId(generateId());
        items.put(item.getId(), item);

        return item;
    }

    @Override
    public Item update(Item item) {
        items.put(item.getId(), item);

        return item;
    }

    @Override
    public Item get(long itemId) {

        return items.get(itemId);
    }

    @Override
    public List<Item> getAll() {

        return new ArrayList<>(items.values());
    }

    @Override
    public void delete(long itemId) {
        items.remove(itemId);
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
