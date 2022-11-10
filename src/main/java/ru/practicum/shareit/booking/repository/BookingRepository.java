package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.Booking;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findAllByBookerIdOrderByIdDesc(Long bookerId);

    List<Booking> findAllByItemId(Long itemId);


    @Query(value = "select * from bookings where item_id in (select id from items where owner_id = ?1) order by id desc", nativeQuery = true)
    List<Booking> findAllByOwner(Long ownerId);


}
