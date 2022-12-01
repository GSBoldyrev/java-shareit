package ru.practicum.shareit.request;

import ru.practicum.shareit.item.dto.ItemDtoShort;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoFull;
import ru.practicum.shareit.request.model.ItemRequest;

import java.time.LocalDateTime;
import java.util.List;

public class ItemRequestMapper {

    public static ItemRequest toItemRequest(ItemRequestDto dto) {
        return new ItemRequest(null,
                dto.getDescription(),
                null,
                LocalDateTime.now());
    }

    public static ItemRequestDto toItemRequestDto(ItemRequest request) {
        return new ItemRequestDto(request.getId(),
                request.getDescription(),
                request.getCreated());
    }

    public static ItemRequestDtoFull toItemRequestDtoFull(ItemRequest request, List<ItemDtoShort> items) {
        return new ItemRequestDtoFull(request.getId(),
                request.getDescription(),
                request.getCreated(),
                items);
    }
}
