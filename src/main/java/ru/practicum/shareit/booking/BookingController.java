package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDtoIncome;
import ru.practicum.shareit.booking.dto.BookingDtoOutcome;
import ru.practicum.shareit.misc.Marker;

import javax.validation.Valid;
import javax.validation.constraints.Min;
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
                                        @RequestBody @Valid BookingDtoIncome bookingDto) {
        return service.add(bookingDto, userId);
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
                                                      @RequestParam(defaultValue = "ALL") String state,
                                                      @RequestParam(defaultValue = "0") @Min(0) int from,
                                                      @RequestParam(defaultValue = "100") @Min(1) int size) {
        return service.getForUser(userId, state, from, size);
    }

    @GetMapping("/owner")
    public List<BookingDtoOutcome> getBookingsForOwner(@RequestHeader("X-Sharer-User-Id") long userId,
                                                       @RequestParam(defaultValue = "ALL") String state,
                                                       @RequestParam(defaultValue = "0") @Min(0) int from,
                                                       @RequestParam(defaultValue = "100") @Min(1) int size) {
        return service.getForOwner(userId, state, from, size);
    }
}
