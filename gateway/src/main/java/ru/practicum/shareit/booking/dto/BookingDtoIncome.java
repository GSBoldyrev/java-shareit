package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@AllArgsConstructor
@Data
public class BookingDtoIncome {
    @Future
    private LocalDateTime start;
    @Future
    private LocalDateTime end;
    @NotNull
    private Long itemId;
}
