package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository {

    Item add(Item item);

    Item update(Item item);

    Item get(long itemId);

    List<Item> getAll();

    void delete(long itemId);

    List<Item> search(String query);
}
