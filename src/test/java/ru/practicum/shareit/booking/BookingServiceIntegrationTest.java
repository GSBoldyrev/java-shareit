package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDtoIncome;
import ru.practicum.shareit.booking.dto.BookingDtoOutcome;
import ru.practicum.shareit.booking.dto.BookingDtoShort;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.error.exception.BadRequestException;
import ru.practicum.shareit.error.exception.NotFoundException;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ru.practicum.shareit.booking.model.Status.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Transactional

class BookingServiceIntegrationTest {

    private final EntityManager em;
    private final BookingService service;
    private final LocalDateTime timestamp1 = LocalDateTime.of(2022, 12, 23, 10, 30);
    private final LocalDateTime timestamp2 = LocalDateTime.of(2022, 12, 23, 11, 30);
    private final BookingDtoIncome income = new BookingDtoIncome(timestamp1, timestamp2, 1L);

    @Test
    void add() {
        service.add(income, 2L);

        TypedQuery<Booking> query = em.createQuery("SELECT b FROM Booking b WHERE b.id = :id", Booking.class);
        Booking booking = query
                .setParameter("id", 7L)
                .getSingleResult();

        assertThat(booking.getStart(), equalTo(timestamp1));
        assertThat(booking.getEnd(), equalTo(timestamp2));
        assertThat(booking.getStatus(), equalTo(WAITING));
    }

    @Test
    void addFailUserNotFound() {
        NotFoundException e = assertThrows(NotFoundException.class,
                () -> service.add(income, 117L));
        assertThat(e.getMessage(), equalTo("Пользователь по ID 117 не найден!"));
    }

    @Test
    void addFailItemNotFound() {
        income.setItemId(64L);
        NotFoundException e = assertThrows(NotFoundException.class,
                () -> service.add(income, 2L));
        assertThat(e.getMessage(), equalTo("Вещь по ID 64 не найдена!"));
    }

    @Test
    void addFailOwnerBooking() {
        NotFoundException e = assertThrows(NotFoundException.class,
                () -> service.add(income, 1L));
        assertThat(e.getMessage(), equalTo("Собрались бронировать собственную вещь?"));
    }

    @Test
    void addFailItemNotAvailable() {
        income.setItemId(5L);
        BadRequestException e = assertThrows(BadRequestException.class,
                () -> service.add(income, 1L));

        assertThat(e.getMessage(), equalTo("Кто-то успел забронировать раньше вас!"));
    }

    @Test
    void addFailEndBeforeStart() {
        income.setStart(timestamp2);
        income.setEnd(timestamp1);
        BadRequestException e = assertThrows(BadRequestException.class,
                () -> service.add(income, 2L));

        assertThat(e.getMessage(), equalTo("Сдать вещь раньше, чем взять ее никак не выйдет!"));
    }

    @Test
    void approve() {
        BookingDtoOutcome booking1 = service.approve(1L, 2L, true);
        BookingDtoOutcome booking2 = service.approve(3L, 4L, false);

        assertThat(booking1.getStatus(), equalTo(APPROVED));
        assertThat(booking2.getStatus(), equalTo(REJECTED));
    }

    @Test
    void approveFailBookingNotFound() {
        NotFoundException e = assertThrows(NotFoundException.class,
                () -> service.approve(1L, 41L, true));
        assertThat(e.getMessage(), equalTo("Бронирование по ID 41 не найдено!"));
    }

    @Test
    void approveFailWrongOwner() {
        NotFoundException e = assertThrows(NotFoundException.class,
                () -> service.approve(2L, 2L, true));
        assertThat(e.getMessage(), equalTo("Это не ваша вещь, вы не можете менять статус бронирования"));
    }

    @Test
    void approveFailWrongStatus() {
        BadRequestException e = assertThrows(BadRequestException.class,
                () -> service.approve(1L, 1L, true));
        assertThat(e.getMessage(), equalTo("Вы уже изменили статус бронирования"));
    }

    @Test
    void get() {
        BookingDtoOutcome booking1 = service.get(1L, 1L);
        BookingDtoOutcome booking2 = service.get(2L, 1L);

        assertThat(booking1, equalTo(booking2));
        assertThat(booking1.getStatus(), equalTo(APPROVED));
    }

    @Test
    void getFailBookingNotFound() {
        NotFoundException e = assertThrows(NotFoundException.class,
                () -> service.get(2L, 222L));
        assertThat(e.getMessage(), equalTo("Бронирование по ID 222 не найдено!"));
    }

    @Test
    void getFailWrongRequest() {
        NotFoundException e = assertThrows(NotFoundException.class,
                () -> service.get(4L, 1L));
        assertThat(e.getMessage(), equalTo("Бронирование к вам не относится."));
    }

    @Test
    void getForUserByStateStatus() {
        List<BookingDtoOutcome> bookings = service.getForUser(1L, "WAITING", 0, 2);

        assertThat(bookings.size(), equalTo(1));
        assertThat(bookings.get(0).getId(), equalTo(4L));
    }

    @Test
    void getForUserByStateTimePast() {
        List<BookingDtoOutcome> bookings = service.getForUser(1L, "PAST", 0, 2);

        assertThat(bookings.size(), equalTo(1));
        assertThat(bookings.get(0).getId(), equalTo(3L));
    }

    @Test
    void getForUserByStateTimeFuture() {
        List<BookingDtoOutcome> bookings = service.getForUser(3L, "FUTURE", 0, 2);

        assertThat(bookings.size(), equalTo(1));
        assertThat(bookings.get(0).getId(), equalTo(2L));
    }

    @Test
    void getForUserByStateAll() {
        List<BookingDtoOutcome> bookings = service.getForUser(1L, "ALL", 0, 2);

        assertThat(bookings.size(), equalTo(2));
        assertThat(bookings.get(0).getId(), equalTo(4L));
    }

    @Test
    void getForUserFailByWrongState() {
        BadRequestException e = assertThrows(BadRequestException.class,
                () -> service.getForUser(1L, "ALLIN", 0, 2));
        assertThat(e.getMessage(), equalTo("Unknown state: ALLIN"));
    }

    @Test
    void getForUserFailWrongUser() {
        NotFoundException e = assertThrows(NotFoundException.class,
                () -> service.getForUser(134L, "ALL", 0, 2));
        assertThat(e.getMessage(), equalTo("Пользователь по ID 134 не найден"));
    }

    @Test
    void getForOwnerByStateStatus() {
        List<BookingDtoOutcome> bookings = service.getForOwner(1L, "WAITING", 0, 2);

        assertThat(bookings.size(), equalTo(1));
        assertThat(bookings.get(0).getId(), equalTo(2L));
    }

    @Test
    void getForOwnerByStateTimePast() {
        List<BookingDtoOutcome> bookings = service.getForOwner(1L, "PAST", 0, 2);

        assertThat(bookings.size(), equalTo(2));
        assertThat(bookings.get(0).getId(), equalTo(5L));
    }

    @Test
    void getForOwnerByStateTimeFuture() {
        List<BookingDtoOutcome> bookings = service.getForOwner(1L, "FUTURE", 0, 2);

        assertThat(bookings.size(), equalTo(2));
        assertThat(bookings.get(0).getId(), equalTo(6L));
    }

    @Test
    void getForOwnerByStateAll() {
        List<BookingDtoOutcome> bookings = service.getForOwner(1L, "ALL", 0, 6);

        assertThat(bookings.size(), equalTo(4));
        assertThat(bookings.get(0).getId(), equalTo(6L));
    }

    @Test
    void getForOwnerFailByWrongState() {
        BadRequestException e = assertThrows(BadRequestException.class,
                () -> service.getForOwner(1L, "ALLIN", 0, 2));
        assertThat(e.getMessage(), equalTo("Unknown state: ALLIN"));
    }

    @Test
    void getForOwnerFailWrongUser() {
        NotFoundException e = assertThrows(NotFoundException.class,
                () -> service.getForOwner(134L, "ALL", 0, 2));
        assertThat(e.getMessage(), equalTo("Пользователь по ID 134 не найден"));
    }

    @Test
    void getNextBookingForItem() {
        BookingDtoShort booking1 = service.getNextBookingForItem(1L);
        BookingDtoShort booking2 = service.getNextBookingForItem(111L);

        assertThat(booking1.getId(), equalTo(2L));
        assertThat(booking2, nullValue());
    }

    @Test
    void getLastBookingForItem() {
        BookingDtoShort booking1 = service.getLastBookingForItem(1L);
        BookingDtoShort booking2 = service.getLastBookingForItem(111L);

        assertThat(booking1.getId(), equalTo(5L));
        assertThat(booking2, nullValue());
    }
}