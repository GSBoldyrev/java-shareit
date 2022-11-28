package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Page<Booking> findAllByBookerIdOrderByIdDesc(Long bookerId, Pageable page);

    Page<Booking> findAllByBookerIdAndStatusOrderByIdDesc(Long bookerId, Status status, Pageable page);

    Page<Booking> findAllByBookerIdAndStartAfterOrderByIdDesc(Long bookerId, LocalDateTime time, Pageable page);

    Page<Booking> findAllByBookerIdAndEndBeforeOrderByIdDesc(Long bookerId, LocalDateTime time, Pageable page);

    Page<Booking> findAllByBookerIdAndStartBeforeAndEndAfterOrderByIdDesc(Long bookerId,
                                                                          LocalDateTime time1,
                                                                          LocalDateTime time2,
                                                                          Pageable page);

    Optional<Booking> findFirstByItemIdAndStartAfterOrderByStartAsc(Long itemId, LocalDateTime start);

    Optional<Booking> findFirstByItemIdAndEndBeforeOrderByEndDesc(Long itemId, LocalDateTime end);

    @Query(value = "SELECT * FROM bookings WHERE item_id IN " +
            "(SELECT id FROM items WHERE owner_id = ?1) ORDER BY id DESC", nativeQuery = true)
    Page<Booking> findAllByOwner(Long ownerId, Pageable page);

    @Query(value = "SELECT * FROM bookings WHERE item_id IN " +
            "(SELECT id FROM items WHERE owner_id = ?1) " +
            "AND status = ?2 ORDER BY id DESC", nativeQuery = true)
    Page<Booking> findAllByOwnerAndStatus(Long ownerId, int status, Pageable page);

    @Query(value = "SELECT * FROM bookings WHERE item_id IN " +
            "(SELECT id FROM items WHERE owner_id = ?1) " +
            "AND start_time > ?2 ORDER BY id DESC", nativeQuery = true)
    Page<Booking> findAllByOwnerForFuture(Long ownerId, LocalDateTime time, Pageable page);

    @Query(value = "SELECT * FROM bookings WHERE item_id IN " +
            "(SELECT id FROM items WHERE owner_id = ?1) " +
            "AND end_time < ?2 ORDER BY id DESC", nativeQuery = true)
    Page<Booking> findAllByOwnerForPast(Long ownerId, LocalDateTime time, Pageable page);

    @Query(value = "SELECT * FROM bookings WHERE item_id IN " +
            "(SELECT id FROM items WHERE owner_id = ?1) " +
            "AND end_time > ?2 " +
            "AND start_time < ?2 ORDER BY id DESC", nativeQuery = true)
    Page<Booking> findAllByOwnerForCurrent(Long ownerId, LocalDateTime time, Pageable page);
}
