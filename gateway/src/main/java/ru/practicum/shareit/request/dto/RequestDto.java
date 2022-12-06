package ru.practicum.shareit.request.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class RequestDto {

    private Long id;
    @NotBlank
    private String description;
    private LocalDateTime created;
}
