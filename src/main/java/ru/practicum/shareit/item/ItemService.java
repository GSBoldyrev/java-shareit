package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.dto.BookingDtoOutcome;
import ru.practicum.shareit.booking.dto.BookingDtoShort;
import ru.practicum.shareit.error.exception.BadRequestException;
import ru.practicum.shareit.error.exception.ConflictException;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoShort;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.practicum.shareit.item.CommentMapper.toComment;
import static ru.practicum.shareit.item.CommentMapper.toCommentDto;
import static ru.practicum.shareit.item.ItemMapper.*;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository repository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final BookingService bookingService;

    public ItemDto getById(long itemId, long userId) {
        Item item = repository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь " + itemId + " не найдена!"));
        List<CommentDto> comments = commentRepository.findAllByItemId(itemId).stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
        if (item.getOwnerId() != userId) {
            return toItemDto(item, null, null, comments);
        }
        BookingDtoShort lastBooking = bookingService.getLastBookingForItem(itemId);
        BookingDtoShort nextBooking = bookingService.getNextBookingForItem(itemId);

        return toItemDto(item, lastBooking, nextBooking, comments);
    }

    public List<ItemDto> getAll(long userId) {
        List<Item> items = repository.findAllByOwnerId(userId);
        List<ItemDto> result = new ArrayList<>();
        for (Item item : items) {
            BookingDtoShort lastBooking = bookingService.getLastBookingForItem(item.getId());
            BookingDtoShort nextBooking = bookingService.getNextBookingForItem(item.getId());
            List<CommentDto> comments = commentRepository.findAllByItemId(item.getId()).stream()
                    .map(CommentMapper::toCommentDto)
                    .collect(Collectors.toList());
            result.add(toItemDto(item, lastBooking, nextBooking, comments));
        }

        return result;
    }

    public ItemDtoShort add(ItemDto itemDto, long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }
        Item item = toItem(itemDto);
        item.setOwnerId(userId);

        return toItemDtoShort(repository.save(item));
    }

    public ItemDtoShort update(ItemDto itemDto, long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }
        Item item = toItem(itemDto);
        long id = item.getId();
        Optional<Item> optionalItem = repository.findById(id)
                .filter(i -> i.getOwnerId() == userId);
        Item itemToUpdate = optionalItem
                .orElseThrow(() -> new NotFoundException("Вещь  " + id + " не найдена!"));
        if (item.getName() != null) {
            itemToUpdate.setName(item.getName());
        }
        if (item.getDescription() != null) {
            itemToUpdate.setDescription(item.getDescription());
        }
        if (item.getAvailable() != null) {
            itemToUpdate.setAvailable(item.getAvailable());
        }

        return toItemDtoShort(repository.save(itemToUpdate));
    }

    public void delete(long itemId, long userId) {
        Optional<Item> optionalItem = repository.findById(itemId)
                .filter(i -> i.getOwnerId() == userId);
        if (optionalItem.isPresent()) {
            repository.deleteById(itemId);
        } else {
            throw new ConflictException("Это ведь не ваша вещь, чтоб ее удалять!");
        }
    }

    public List<ItemDtoShort> search(String text) {
        if (text.isBlank()) {
            return new ArrayList<>();
        }
        String query = text.toLowerCase();
        List<Item> items = repository.search(query);
        if (items.isEmpty()) {
            throw new NotFoundException("Искомая вещь не найдена!");
        }

        return items.stream().map(ItemMapper::toItemDtoShort).collect(Collectors.toList());
    }

    public CommentDto addComment(CommentDto commentDto, long itemId, long userId) {
        List<BookingDtoOutcome> bookings = bookingService.getForUser(userId, "PAST").stream()
                .filter(b -> b.getItem().getId() == itemId)
                .collect(Collectors.toList());
        if (bookings.isEmpty()) {
            throw new BadRequestException("Пользователь не может оставить отзыв об этой вещи");
        }
        Comment comment = toComment(commentDto);
        comment.setAuthor(userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь по ID " + userId + " не найден!")));
        comment.setItem(repository.findById(itemId).orElseThrow(() -> new NotFoundException("Вещь " + itemId + " не найдена!")));
        commentRepository.save(comment);

        return toCommentDto(comment);
    }
}
