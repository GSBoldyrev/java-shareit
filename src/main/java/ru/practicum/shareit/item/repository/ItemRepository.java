package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query(value = "SELECT * FROM items " +
            "WHERE available = TRUE AND " +
            "(LOWER(name) LIKE '%' || ?1 || '%' OR LOWER(description) LIKE '%' || ?1 || '%')",
            nativeQuery = true)
    List<Item> search(String search);

    List<Item> findAllByOwnerId(Long ownerId);
}
