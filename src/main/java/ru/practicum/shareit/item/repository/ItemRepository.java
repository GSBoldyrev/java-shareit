package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.misc.CrudRepository;

import java.util.List;

public interface ItemRepository extends CrudRepository<Item> {

    List<Item> search(String query);
}
