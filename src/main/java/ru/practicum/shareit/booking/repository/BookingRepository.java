package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findAllByBookerIdOrderByIdDesc(Long bookerId);

    List<Booking> findAllByBookerIdAndStatusOrderByIdDesc(Long bookerId, Status status);

    List<Booking> findAllByBookerIdAndStartAfterOrderByIdDesc(Long bookerId, LocalDateTime time);

    List<Booking> findAllByBookerIdAndEndBeforeOrderByIdDesc(Long bookerId, LocalDateTime time);

    List<Booking> findAllByBookerIdAndStartBeforeAndEndAfterOrderByIdDesc(Long bookerId,
                                                                          LocalDateTime time1,
                                                                          LocalDateTime time2);

    Optional<Booking> findFirstByItemIdAndStartAfterOrderByStartAsc(Long itemId, LocalDateTime start);

    Optional<Booking> findFirstByItemIdAndEndBeforeOrderByEndDesc(Long itemId, LocalDateTime end);

    @Query(value = "SELECT * FROM bookings WHERE item_id IN " +
            "(SELECT id FROM items WHERE owner_id = ?1) ORDER BY id DESC", nativeQuery = true)
    List<Booking> findAllByOwner(Long ownerId);

    @Query(value = "SELECT * FROM bookings WHERE item_id IN " +
            "(SELECT id FROM items WHERE owner_id = ?1) " +
            "AND status = ?2 ORDER BY id DESC", nativeQuery = true)
    List<Booking> findAllByOwnerAndStatus(Long ownerId, int status);

    @Query(value = "SELECT * FROM bookings WHERE item_id IN " +
            "(SELECT id FROM items WHERE owner_id = ?1) " +
            "AND start_time > ?2 ORDER BY id DESC", nativeQuery = true)
    List<Booking> findAllByOwnerForFuture(Long ownerId, LocalDateTime time);

    @Query(value = "SELECT * FROM bookings WHERE item_id IN " +
            "(SELECT id FROM items WHERE owner_id = ?1) " +
            "AND end_time < ?2 ORDER BY id DESC", nativeQuery = true)
    List<Booking> findAllByOwnerForPast(Long ownerId, LocalDateTime time);

    @Query(value = "SELECT * FROM bookings WHERE item_id IN " +
            "(SELECT id FROM items WHERE owner_id = ?1) " +
            "AND end_time > ?2 " +
            "AND start_time < ?2 ORDER BY id DESC", nativeQuery = true)
    List<Booking> findAllByOwnerForCurrent(Long ownerId, LocalDateTime time);
}
