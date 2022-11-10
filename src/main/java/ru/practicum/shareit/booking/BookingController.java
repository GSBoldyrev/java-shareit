package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDtoIncome;
import ru.practicum.shareit.booking.dto.BookingDtoOutcome;
import ru.practicum.shareit.misc.Marker;
import ru.practicum.shareit.misc.State;

import javax.validation.Valid;
import java.util.List;


@RestController
@Validated
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService service;

    @PostMapping
    @Validated({Marker.OnCreate.class})
    public BookingDtoOutcome addBooking(@RequestHeader("X-Sharer-User-Id") long userId,
                                        @RequestBody @Valid BookingDtoIncome BookingDto) {
        return service.add(BookingDto, userId);
    }

    @PatchMapping("/{bookingId}")
    public BookingDtoOutcome approveBooking(@RequestHeader("X-Sharer-User-Id") long userId,
                                     @PathVariable long bookingId,
                                     @RequestParam boolean approved) {
        return service.approve(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDtoOutcome getBooking(@RequestHeader("X-Sharer-User-Id") long userId,
                                 @PathVariable long bookingId) {
        return service.get(userId, bookingId);
    }

    @GetMapping
    public List<BookingDtoOutcome> getBookingsForUser(@RequestHeader("X-Sharer-User-Id") long userId,
                                               @RequestParam(defaultValue = "ALL") String state) {
        return service.getForUser(userId, state);
    }

    @GetMapping("/owner")
    public List<BookingDtoOutcome> getBookingsForOwner(@RequestHeader("X-Sharer-User-Id") long userId,
                                                @RequestParam(defaultValue = "ALL") String state) {
        return service.getForOwner(userId, state);
    }
}
