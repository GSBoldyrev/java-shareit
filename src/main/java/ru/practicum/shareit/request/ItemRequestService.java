package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemDtoShort;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoFull;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.request.ItemRequestMapper.*;

@Service
@RequiredArgsConstructor
public class ItemRequestService {

    private final ItemRepository itemRepository;
    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;

    public ItemRequestDto add(ItemRequestDto dto, Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }
        ItemRequest request = toItemRequest(dto);
        request.setRequestorId(userId);

        return toItemRequestDto(requestRepository.save(request));
    }

    public ItemRequestDtoFull getById(Long userId, Long requestId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }
        ItemRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос " + requestId + " не найден!"));

        return getRequestDto(request);
    }

    public List<ItemRequestDtoFull> getForAuthor(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }
        List<ItemRequest> requests = requestRepository.findAllByRequestorId(userId);

        return requests.stream()
                .map(this::getRequestDto)
                .collect(Collectors.toList());
    }

    public List<ItemRequestDtoFull> getAll(Long userId, int from, int size) {
        Pageable page = PageRequest.of(from/size, size, Sort.by("created").ascending());

        return requestRepository.findAllByRequestorIdIsNot(userId, page).stream().map(this::getRequestDto).collect(Collectors.toList());
    }

    private ItemRequestDtoFull getRequestDto(ItemRequest request) {
        List<ItemDtoShort> items = itemRepository.findAllByRequestId(request.getRequestorId()).stream()
                .map(ItemMapper::toItemDtoShort)
                .collect(Collectors.toList());

        return toItemRequestDtoFull(request, items);
    }
}
