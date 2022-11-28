package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDtoIncome;
import ru.practicum.shareit.booking.dto.BookingDtoOutcome;
import ru.practicum.shareit.booking.dto.BookingDtoShort;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.error.exception.BadRequestException;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.booking.BookingMapper.toBooking;
import static ru.practicum.shareit.booking.BookingMapper.toBookingDto;
import static ru.practicum.shareit.booking.model.Status.*;

@Service
@RequiredArgsConstructor
public class BookingService {

    public final BookingRepository bookingRepository;
    public final ItemRepository itemRepository;
    public final UserRepository userRepository;

    public BookingDtoOutcome add(BookingDtoIncome bookingDto, long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь по ID " + userId + " не найден!"));
        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь по ID " + bookingDto.getItemId() + " не найдена!"));
        if (userId == item.getOwnerId()) {
            throw new NotFoundException("Собрались бронировать собственную вещь?");
        }
        if (!item.getAvailable()) {
            throw new BadRequestException("Кто-то успел забронировать раньше вас!");
        }
        LocalDateTime start = bookingDto.getStart();
        LocalDateTime end = bookingDto.getEnd();
        if (end.isBefore(start)) {
            throw new BadRequestException("Сдать вещь раньше, чем взять ее никак не выйдет!");
        }
        Booking booking = toBooking(bookingDto, user, item);
        booking.setStatus(WAITING);
        Booking savedBooking = bookingRepository.save(booking);

        return toBookingDto(savedBooking);
    }

    public BookingDtoOutcome approve(long userId, long bookingId, boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование на найдено!"));
        if (booking.getItem().getOwnerId() != userId) {
            throw new NotFoundException("Это не ваша вещь, вы не можете менять статус бронирования");
        }
        if (!booking.getStatus().equals(WAITING)) {
            throw new BadRequestException("Вы уже изменили статус бронирования");
        }
        if (approved) {
            booking.setStatus(APPROVED);
        } else {
            booking.setStatus(REJECTED);
        }

        return toBookingDto(bookingRepository.save(booking));
    }

    public BookingDtoOutcome get(long userId, long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование на найдено!"));
        if (userId != booking.getBooker().getId() && userId != booking.getItem().getOwnerId()) {
            throw new NotFoundException("Бронирование к вам не относится.");
        }

        return toBookingDto(booking);
    }

    public List<BookingDtoOutcome> getForUser(long userId, String state, int from, int size) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }
        Page<Booking> bookings;
        Pageable page = PageRequest.of(from/size, size);
        switch (state) {
            case "PAST":
                bookings = bookingRepository.findAllByBookerIdAndEndBeforeOrderByIdDesc(userId, LocalDateTime.now(), page);
                break;
            case "FUTURE":
                bookings = bookingRepository.findAllByBookerIdAndStartAfterOrderByIdDesc(userId, LocalDateTime.now(), page);
                break;
            case "CURRENT":
                bookings = bookingRepository.findAllByBookerIdAndStartBeforeAndEndAfterOrderByIdDesc(userId,
                        LocalDateTime.now(),
                        LocalDateTime.now(), page);
                break;
            case "ALL":
                bookings = bookingRepository.findAllByBookerIdOrderByIdDesc(userId, page);
                break;
            case "WAITING":
                bookings = bookingRepository.findAllByBookerIdAndStatusOrderByIdDesc(userId, WAITING, page);
                break;
            case "REJECTED":
                bookings = bookingRepository.findAllByBookerIdAndStatusOrderByIdDesc(userId, REJECTED, page);
                break;
            default:
                throw new BadRequestException("Unknown state: " + state);
        }

        return bookings.stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    public List<BookingDtoOutcome> getForOwner(long userId, String state, int from, int size) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }
        Page<Booking> bookings;
        Pageable page = PageRequest.of(from/size, size);
        switch (state) {
            case "PAST":
                bookings = bookingRepository.findAllByOwnerForPast(userId, LocalDateTime.now(), page);
                break;
            case "FUTURE":
                bookings = bookingRepository.findAllByOwnerForFuture(userId, LocalDateTime.now(), page);
                break;
            case "CURRENT":
                bookings = bookingRepository.findAllByOwnerForCurrent(userId, LocalDateTime.now(), page);
                break;
            case "ALL":
                bookings = bookingRepository.findAllByOwner(userId, page);
                break;
            case "WAITING":
                bookings = bookingRepository.findAllByOwnerAndStatus(userId, WAITING.ordinal(), page);
                break;
            case "REJECTED":
                bookings = bookingRepository.findAllByOwnerAndStatus(userId, REJECTED.ordinal(), page);
                break;
            default:
                throw new BadRequestException("Unknown state: " + state);
        }

        return bookings.stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    public BookingDtoShort getNextBookingForItem(long itemId) {
        return bookingRepository.findFirstByItemIdAndStartAfterOrderByStartAsc(itemId, LocalDateTime.now())
                .map(BookingMapper::toBookingDtoShort)
                .orElse(null);
    }

    public BookingDtoShort getLastBookingForItem(long itemId) {
        return bookingRepository.findFirstByItemIdAndEndBeforeOrderByEndDesc(itemId, LocalDateTime.now())
                .map(BookingMapper::toBookingDtoShort)
                .orElse(null);
    }
}
