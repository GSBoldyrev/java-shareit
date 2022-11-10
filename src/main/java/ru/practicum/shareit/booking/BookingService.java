package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDtoIncome;
import ru.practicum.shareit.booking.dto.BookingDtoOutcome;
import ru.practicum.shareit.booking.dto.BookingDtoShort;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.error.exception.BadRequestException;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.practicum.shareit.booking.BookingMapper.toBooking;
import static ru.practicum.shareit.booking.BookingMapper.toBookingDto;

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
                .orElseThrow(() -> new NotFoundException("Вещь по ID" + bookingDto.getItemId() + " не найдена!"));
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
        booking.setStatus(Status.WAITING);
        Booking savedBooking = bookingRepository.save(booking);

        return toBookingDto(savedBooking);
    }

    public BookingDtoOutcome approve(long userId, long bookingId, boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование на найдено!"));
        if (booking.getItem().getOwnerId() != userId) {
            throw new NotFoundException("Это не ваша вещь, вы не можете менять статус бронирования");
        }
        if (!booking.getStatus().equals(Status.WAITING)) {
            throw new BadRequestException("Вы уже изменили статус бронирования");
        }
        if (approved) {
            booking.setStatus(Status.APPROVED);
        } else {
            booking.setStatus(Status.REJECTED);
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

    public List<BookingDtoOutcome> getForUser(long userId, String state) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }

        return filterByState(bookingRepository.findAllByBookerIdOrderByIdDesc(userId), state);
    }

    public List<BookingDtoOutcome> getForOwner(long userId, String state) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }

        return filterByState(bookingRepository.findAllByOwner(userId), state);
    }

    public BookingDtoShort getNextBookingForItem(long itemId) {
        Optional<BookingDtoShort> nextBooking = bookingRepository.findAllByItemId(itemId).stream()
                .map(BookingMapper::toBookingDtoShort)
                .filter(b -> b.getStart().isAfter(LocalDateTime.now()))
                .min(Comparator.comparing(BookingDtoShort::getStart));

        return nextBooking.orElse(null);
    }

    public BookingDtoShort getLastBookingForItem(long itemId) {
        Optional<BookingDtoShort> lastBooking = bookingRepository.findAllByItemId(itemId).stream()
                .map(BookingMapper::toBookingDtoShort)
                .filter(b -> b.getEnd().isBefore(LocalDateTime.now()))
                .max(Comparator.comparing(BookingDtoShort::getEnd));

        return lastBooking.orElse(null);
    }

    private List<BookingDtoOutcome> filterByState(List<Booking> bookings, String state) {
        Stream<BookingDtoOutcome> toFilter = bookings.stream()
                .map(BookingMapper::toBookingDto);
        switch (state) {
            case "PAST":
                return toFilter
                        .filter(b -> b.getEnd().isBefore(LocalDateTime.now()))
                        .collect(Collectors.toList());
            case "FUTURE":
                return toFilter
                        .filter(b -> b.getStart().isAfter(LocalDateTime.now()))
                        .collect(Collectors.toList());
            case "CURRENT":
                return toFilter
                        .filter(b -> b.getEnd().isAfter(LocalDateTime.now()))
                        .filter(b -> b.getStart().isBefore(LocalDateTime.now()))
                        .collect(Collectors.toList());
            case "WAITING":
                return toFilter
                        .filter(b -> b.getStatus().equals(Status.WAITING))
                        .collect(Collectors.toList());
            case "REJECTED":
                return toFilter
                        .filter(b -> b.getStatus().equals(Status.REJECTED))
                        .collect(Collectors.toList());
            case "ALL":
                return toFilter.collect(Collectors.toList());
            default:
                throw new BadRequestException("Unknown state: " + state);
        }
    }
}
