package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.misc.Marker;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoFull;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
public class ItemRequestController {

    private final ItemRequestService service;

    @PostMapping
    @Validated({Marker.OnCreate.class})
    public ItemRequestDto addRequest(@RequestHeader("X-Sharer-User-Id") long userId,
                                     @RequestBody @Valid ItemRequestDto requestDto) {
        return service.add(requestDto, userId);
    }

    @GetMapping
    public List<ItemRequestDtoFull> getForAuthor(@RequestHeader("X-Sharer-User-Id") long userId) {
        return service.getForAuthor(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDtoFull> getAll(@RequestHeader("X-Sharer-User-Id") long userId,
                                           @RequestParam(defaultValue = "0") @Min(0) int from,
                                           @RequestParam(defaultValue = "100") @Min(1) int size) {
        return service.getAll(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDtoFull getById(@RequestHeader("X-Sharer-User-Id") long userId,
                                  @PathVariable long requestId) {
        return service.getById(userId, requestId);
    }
}
