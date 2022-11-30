package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDtoShort;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoFull;

import java.time.LocalDateTime;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
@AutoConfigureWebMvc
class ItemRequestControllerTest {

    @Autowired
    ObjectMapper mapper;

    @MockBean
    ItemRequestService service;

    @Autowired
    private MockMvc mvc;

    private final LocalDateTime timestamp1 = LocalDateTime.of(2022, 11, 23, 10, 30);
    private final LocalDateTime timestamp2 = LocalDateTime.of(2022, 11, 23, 11, 30);
    private final LocalDateTime timestamp3 = LocalDateTime.of(2022, 11, 23, 12, 30);
    private final ItemRequestDto dto = new ItemRequestDto(1L, "description", timestamp1);
    private final ItemDtoShort item1 = new ItemDtoShort(10L, "i1", "d1", true, 2L);
    private final ItemDtoShort item2 = new ItemDtoShort(10L, "i2", "d2", true, 2L);
    private final ItemRequestDtoFull full1 = new ItemRequestDtoFull(2L,
            "description1",
            timestamp2,
            List.of(item1, item2));
    private final ItemRequestDtoFull full2 = new ItemRequestDtoFull(3L,
            "description2",
            timestamp3,
            List.of(item1, item2));

    @Test
    void addRequestWithStatusOk() throws Exception {
        when(service.add(any(), anyLong()))
                .thenReturn(dto);

        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(UTF_8)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(dto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(dto.getDescription())));

        verify(service, times(1)).add(dto, 1L);
    }

    @Test
    void addRequestWithWrongUserAndStatusNotFound() throws Exception {
        when(service.add(any(), anyLong()))
                .thenThrow(new NotFoundException("Пользователь не найден!"));

        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(UTF_8)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Пользователь не найден!")))
                .andExpect(jsonPath("$.description", is("Not found exception")));

        verify(service, times(1)).add(dto, 1L);
    }

    @Test
    void addRequestWithoutDescriptionAndStatusBadRequest() throws Exception {
        ItemRequestDto noDescription = new ItemRequestDto(1L, null, timestamp1);

        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(noDescription))
                        .characterEncoding(UTF_8)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error",
                        is("addRequest.requestDto.description: must not be blank")))
                .andExpect(jsonPath("$.description", is("Validation Exception")));

        verifyNoInteractions(service);
    }

    @Test
    void getForAuthorWithStatusOk() throws Exception {
        when(service.getForAuthor(1L)).thenReturn(List.of(full1, full2));

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].description", is(full1.getDescription())))
                .andExpect(jsonPath("$[1].description", is(full2.getDescription())));

        verify(service, times(1)).getForAuthor(1L);
    }

    @Test
    void getForAuthorWithWrongIdWithNotFound() throws Exception {
        when(service.getForAuthor(anyLong()))
                .thenThrow(new NotFoundException("Пользователь не найден!"));

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Пользователь не найден!")))
                .andExpect(jsonPath("$.description", is("Not found exception")));

        verify(service, times(1)).getForAuthor(1L);
    }

    @Test
    void getAllWithStatusOk() throws Exception {
        when(service.getAll(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(full1, full2));

        mvc.perform(get("/requests/all?from=2&size=2")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].description", is(full1.getDescription())))
                .andExpect(jsonPath("$[1].description", is(full2.getDescription())));

        verify(service, times(1)).getAll(1L, 2, 2);
    }

    @Test
    void getAllWithWrongFromWithStatusBadRequest() throws Exception {
        mvc.perform(get("/requests/all?from=-2&size=2")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("getAll.from: must be greater than or equal to 0")))
                .andExpect(jsonPath("$.description", is("Validation Exception")));

        verifyNoInteractions(service);
    }

    @Test
    void getAllWithWrongSizeWithStatusBadRequest() throws Exception {
        mvc.perform(get("/requests/all?from=2&size=0")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("getAll.size: must be greater than or equal to 1")))
                .andExpect(jsonPath("$.description", is("Validation Exception")));

        verifyNoInteractions(service);
    }

    @Test
    void getByIdWithStatusOk() throws Exception {
        when(service.getById(1L, 2L)).thenReturn(full1);

        mvc.perform(get("/requests/2")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(full1.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(full1.getDescription())));

        verify(service, times(1)).getById(1L, 2L);
    }

    @Test
    void getByIdWithStatusNotFound() throws Exception {
        when(service.getById(anyLong(), anyLong()))
                .thenThrow(new NotFoundException("Тестовое исключение"));

        mvc.perform(get("/requests/14")
                        .header("X-Sharer-User-Id", 21L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Тестовое исключение")))
                .andExpect(jsonPath("$.description", is("Not found exception")));

        verify(service, times(1)).getById(21L, 14L);
    }
}