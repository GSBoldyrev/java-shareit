package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureWebMvc
class UserControllerTest {

    @Autowired
    ObjectMapper mapper;

    @MockBean
    UserService service;

    @Autowired
    private MockMvc mvc;

    private final UserDto dto1 = new UserDto(1L, "Alfa", "alfa@yandex.ru");
    private final UserDto dto2 = new UserDto(2L, "Beta", "beta@yandex.ru");

    @Test
    void getByIdAndStatusOk() throws Exception {
        when(service.getById(1L))
                .thenReturn(dto1);

        mvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(dto1.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(dto1.getName())))
                .andExpect(jsonPath("$.email", is(dto1.getEmail())));

        verify(service, times(1)).getById(1L);
    }

    @Test
    void getByIdAndStatusNotFound() throws Exception {
        when(service.getById(17L))
                .thenThrow(new NotFoundException("Пользователь по ID 17 не найден!"));

        mvc.perform(get("/users/17"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Пользователь по ID 17 не найден!")))
                .andExpect(jsonPath("$.description", is("Not found exception")));

        verify(service, times(1)).getById(17L);
    }

    @Test
    void getUsers() throws Exception {
        when(service.getAll())
                .thenReturn(List.of(dto1, dto2));

        mvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is(dto1.getName())))
                .andExpect(jsonPath("$[0].email", is(dto1.getEmail())))
                .andExpect(jsonPath("$[1].name", is(dto2.getName())))
                .andExpect(jsonPath("$[1].email", is(dto2.getEmail())));

        verify(service, times(1)).getAll();
    }

    @Test
    void createUserWithStatusOk() throws Exception {
        when(service.add(any())).thenReturn(dto1);

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(dto1))
                        .characterEncoding(UTF_8)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(dto1.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(dto1.getName())))
                .andExpect(jsonPath("$.email", is(dto1.getEmail())));

        verify(service, times(1)).add(any());
    }

    @Test
    void createUserWithoutNameWithStatusBadRequest() throws Exception {
        UserDto noName = new UserDto(3L, null, "noName@yandex.ru");

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(noName))
                        .characterEncoding(UTF_8)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("createUser.userDto.name: must not be null")))
                .andExpect(jsonPath("$.description", is("Validation Exception")));

        verifyNoInteractions(service);
    }

    @Test
    void createUserWithoutEmailWithStatusBadRequest() throws Exception {
        UserDto noEmail = new UserDto(3L, "noEmail", null);

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(noEmail))
                        .characterEncoding(UTF_8)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("createUser.userDto.email: must not be null")))
                .andExpect(jsonPath("$.description", is("Validation Exception")));

        verifyNoInteractions(service);
    }

    @Test
    void createUserWithWrongEmailWithStatusBadRequest() throws Exception {
        UserDto wrongEmail = new UserDto(3L, "wrongEmail", "wrongEmail%yandex.ru");

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(wrongEmail))
                        .characterEncoding(UTF_8)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error",
                        is("createUser.userDto.email: must be a well-formed email address")))
                .andExpect(jsonPath("$.description", is("Validation Exception")));

        verifyNoInteractions(service);
    }

    @Test
    void updateUserWithStatusOk() throws Exception {
        UserDto validDto = new UserDto(null, "Alfa", "alfa@yandex.ru");

        when(service.update(validDto, 1L)).thenReturn(dto1);

        mvc.perform(patch("/users/1")
                        .content(mapper.writeValueAsString(validDto))
                        .characterEncoding(UTF_8)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(dto1.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(dto1.getName())))
                .andExpect(jsonPath("$.email", is(dto1.getEmail())));

        verify(service, times(1)).update(validDto, 1L);
    }

    @Test
    void updateUserWithWrongIdWithStatusNotFound() throws Exception {
        UserDto validDto = new UserDto(null, "Alfa", "alfa@yandex.ru");

        when(service.update(validDto, 18L))
                .thenThrow(new NotFoundException("Пользователь по ID 18 не найден!"));

        mvc.perform(patch("/users/18")
                        .content(mapper.writeValueAsString(validDto))
                        .characterEncoding(UTF_8)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Пользователь по ID 18 не найден!")))
                .andExpect(jsonPath("$.description", is("Not found exception")));

        verify(service, times(1)).update(validDto, 18L);
    }

    @Test
    void updateUserWithIdWithStatusBadRequest() throws Exception {
        UserDto invalidDto = new UserDto(19L, "Alfa", "alfa@yandex.ru");

        mvc.perform(patch("/users/1")
                        .content(mapper.writeValueAsString(invalidDto))
                        .characterEncoding(UTF_8)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("updateUser.userDto.id: must be null")))
                .andExpect(jsonPath("$.description", is("Validation Exception")));

        verifyNoInteractions(service);
    }

    @Test
    void deleteUser() throws Exception {
        mvc.perform(delete("/users/1"))
                .andExpect(status().isOk());

        verify(service, times(1)).delete(1L);
    }
}