package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDtoIncome;
import ru.practicum.shareit.booking.dto.BookingDtoOutcome;

import java.util.List;


@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {

    private final BookingService service;

    @PostMapping
    public BookingDtoOutcome addBooking(@RequestHeader("X-Sharer-User-Id") long userId,
                                        @RequestBody BookingDtoIncome bookingDto) {
        log.debug("Запрос на создание нового бронирования на вещь {} от пользователя {}",
                bookingDto.getItemId(),
                userId);

        return service.add(bookingDto, userId);
    }

    @PatchMapping("/{bookingId}")
    public BookingDtoOutcome approveBooking(@RequestHeader("X-Sharer-User-Id") long userId,
                                            @PathVariable long bookingId,
                                            @RequestParam boolean approved) {
        log.debug("Запрос на изменение статуса бронирования {}", bookingId);
        return service.approve(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDtoOutcome getBooking(@RequestHeader("X-Sharer-User-Id") long userId,
                                        @PathVariable long bookingId) {
        log.debug("Заарос на просмотр бронирования {} от пользователя {}", bookingId, userId);
        return service.get(userId, bookingId);
    }

    @GetMapping
    public List<BookingDtoOutcome> getBookingsForUser(@RequestHeader("X-Sharer-User-Id") long userId,
                                                      @RequestParam(defaultValue = "ALL") String state,
                                                      @RequestParam(defaultValue = "0") int from,
                                                      @RequestParam(defaultValue = "100") int size) {
        log.debug("Запрос на получение всех бронирований пользователя {}", userId);
        return service.getForUser(userId, state, from, size);
    }

    @GetMapping("/owner")
    public List<BookingDtoOutcome> getBookingsForOwner(@RequestHeader("X-Sharer-User-Id") long userId,
                                                       @RequestParam(defaultValue = "ALL") String state,
                                                       @RequestParam(defaultValue = "0") int from,
                                                       @RequestParam(defaultValue = "100") int size) {
        log.debug("Запрос на получение всех бронирований владельцем забронированных вещей. ID владельца - {}", userId);
        return service.getForOwner(userId, state, from, size);
    }
}
