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
import ru.practicum.shareit.user.model.User;
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

    private final ItemRepository itemRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final BookingService bookingService;

    public ItemDto getById(long itemId, long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь " + itemId + " не найдена!"));
        ItemDto result = toItemDto(item);
        populateItemDto(result);
        if (item.getOwnerId() != userId) {
            result.setLastBooking(null);
            result.setNextBooking(null);
        }

        return result;
    }

    public List<ItemDto> getAll(long userId) {
        return itemRepository.findAllByOwnerId(userId).stream()
                .map(ItemMapper::toItemDto)
                .peek(this::populateItemDto)
                .collect(Collectors.toList());
    }

    public ItemDtoShort add(ItemDto itemDto, long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }
        Item item = toItem(itemDto);
        item.setOwnerId(userId);

        return toItemDtoShort(itemRepository.save(item));
    }

    public ItemDtoShort update(ItemDto itemDto, long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }
        Item item = toItem(itemDto);
        Item itemToUpdate = itemRepository.findById(item.getId())
                .filter(i -> i.getOwnerId() == userId)
                .orElseThrow(() -> new NotFoundException("Вещь  " + item.getId() + " не найдена!"));
        if (item.getName() != null) {
            itemToUpdate.setName(item.getName());
        }
        if (item.getDescription() != null) {
            itemToUpdate.setDescription(item.getDescription());
        }
        if (item.getAvailable() != null) {
            itemToUpdate.setAvailable(item.getAvailable());
        }

        return toItemDtoShort(itemRepository.save(itemToUpdate));
    }

    public void delete(long itemId, long userId) {
        Optional<Item> optionalItem = itemRepository.findById(itemId)
                .filter(i -> i.getOwnerId() == userId);
        if (optionalItem.isPresent()) {
            itemRepository.deleteById(itemId);
        } else {
            throw new ConflictException("Это ведь не ваша вещь, чтоб ее удалять!");
        }
    }

    public List<ItemDtoShort> search(String text) {
        if (text.isBlank()) {
            return new ArrayList<>();
        }
        String query = text.toLowerCase();
        List<Item> items = itemRepository.search(query);
        if (items.isEmpty()) {
            throw new NotFoundException("Искомая вещь не найдена!");
        }

        return items.stream()
                .map(ItemMapper::toItemDtoShort)
                .collect(Collectors.toList());
    }

    public CommentDto addComment(CommentDto commentDto, long itemId, long userId) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь по ID " + userId + " не найден!"));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь по ID" + itemId + " не найдена!"));
        List<BookingDtoOutcome> bookings = bookingService.getForUser(userId, "PAST").stream()
                .filter(b -> b.getItem().getId() == itemId)
                .collect(Collectors.toList());
        if (bookings.isEmpty()) {
            throw new BadRequestException("Пользователь не может оставить отзыв об этой вещи");
        }
        Comment comment = toComment(commentDto);
        comment.setAuthor(author);
        comment.setItem(item);

        return toCommentDto(commentRepository.save(comment));
    }

    private void populateItemDto(ItemDto dto) {
        BookingDtoShort lastBooking = bookingService.getLastBookingForItem(dto.getId());
        BookingDtoShort nextBooking = bookingService.getNextBookingForItem(dto.getId());
        List<CommentDto> comments = commentRepository.findAllByItemId(dto.getId()).stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
        dto.setLastBooking(lastBooking);
        dto.setNextBooking(nextBooking);
        dto.setComments(comments);
    }
}
