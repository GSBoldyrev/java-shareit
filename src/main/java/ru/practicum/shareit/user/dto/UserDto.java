package ru.practicum.shareit.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.shareit.misc.Marker;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

@Data
@AllArgsConstructor
public class UserDto {

    @Null(groups = Marker.OnUpdate.class)
    private Long id;
    @NotNull(groups = Marker.OnCreate.class)
    private String name;
    @NotNull(groups = Marker.OnCreate.class)
    @Email(groups = Marker.OnCreate.class)
    private String email;
}
