package ru.practicum.shareit.request.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.shareit.item.dto.ItemDtoShort;
import ru.practicum.shareit.misc.Marker;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class ItemRequestDtoFull {

    private Long id;
    @NotBlank(groups = Marker.OnCreate.class)
    private String description;
    private LocalDateTime created;
    private List<ItemDtoShort> items;
}
