package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDtoIncome;
import ru.practicum.shareit.booking.dto.BookingDtoOutcome;
import ru.practicum.shareit.booking.dto.BookingDtoShort;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;


public class BookingMapper {



    public static Booking toBooking(BookingDtoIncome bookingDto, User user, Item item) {
        return new Booking(null,
                bookingDto.getStart(),
                bookingDto.getEnd(),
                item,
                user,
                null);
    }

    public static BookingDtoOutcome toBookingDto(Booking booking) {
        return new BookingDtoOutcome(booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                booking.getItem(),
                booking.getBooker(),
                booking.getStatus());
    }

    public static BookingDtoShort toBookingDtoShort(Booking booking) {
        return new BookingDtoShort(booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                booking.getBooker().getId(),
                booking.getStatus());
    }
}
