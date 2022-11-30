package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.error.exception.BadRequestException;
import ru.practicum.shareit.error.exception.ConflictException;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoShort;

import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
@AutoConfigureWebMvc
class ItemControllerTest {

    @Autowired
    ObjectMapper mapper;

    @MockBean
    ItemService service;

    @Autowired
    private MockMvc mvc;

    private final ItemDtoShort dtoShort = new ItemDtoShort(1L,
            "name",
            "description",
            true,
            2L);
    private final ItemDto dto1 = new ItemDto(2L,
            "name2",
            "description2",
            true,
            20L,
            null,
            null,
            null);
    private final ItemDto dto2 = new ItemDto(3L,
            "name3",
            "description3",
            true,
            30L,
            null,
            null,
            null);
    private final CommentDto commentDto = new CommentDto(4L, "text", "author", null);


    @Test
    void getItemsWithStatusOk() throws Exception {
        when(service.getAll(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(dto1, dto2));

        mvc.perform(get("/items?from=2&size=2")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].description", is(dto1.getDescription())))
                .andExpect(jsonPath("$[1].description", is(dto2.getDescription())));

        verify(service, times(1)).getAll(1L, 2, 2);
    }

    @Test
    void getItemsWithWrongFromWithStatusBadRequest() throws Exception {
        mvc.perform(get("/items?from=-2&size=2")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("getItems.from: must be greater than or equal to 0")))
                .andExpect(jsonPath("$.description", is("Validation Exception")));

        verifyNoInteractions(service);
    }

    @Test
    void getItemsWithWrongSizeWithStatusBadRequest() throws Exception {
        mvc.perform(get("/items?from=2&size=0")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("getItems.size: must be greater than or equal to 1")))
                .andExpect(jsonPath("$.description", is("Validation Exception")));

        verifyNoInteractions(service);
    }

    @Test
    void getByIdWithStatusOk() throws Exception {
        when(service.getById(anyLong(), anyLong())).thenReturn(dto1);

        mvc.perform(get("/items/2")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(dto1.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(dto1.getDescription())));

        verify(service, times(1)).getById(2L, 1L);
    }

    @Test
    void getByIdWithWrongIdWithStatusNotFound() throws Exception {
        when(service.getById(anyLong(), anyLong()))
                .thenThrow(new NotFoundException("Вещь 28 не найдена!"));

        mvc.perform(get("/items/28")
                        .header("X-Sharer-User-Id", 82L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Вещь 28 не найдена!")))
                .andExpect(jsonPath("$.description", is("Not found exception")));

        verify(service, times(1)).getById(28L, 82L);
    }

    @Test
    void searchItemWithStatusOk() throws Exception {
        when(service.search(anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(dtoShort));

        mvc.perform(get("/items/search?text=Поиск&from=2&size=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].description", is(dtoShort.getDescription())));

        verify(service, times(1)).search("Поиск", 2, 2);
    }

    @Test
    void searchItemWithWrongFromWithStatusBadRequest() throws Exception {
        mvc.perform(get("/items/search?text=Поиск&from=-2&size=2"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error",
                        is("searchItem.from: must be greater than or equal to 0")))
                .andExpect(jsonPath("$.description", is("Validation Exception")));

        verifyNoInteractions(service);
    }

    @Test
    void searchItemWithWrongSizeWithStatusBadRequest() throws Exception {
        mvc.perform(get("/items/search?text=Поиск&from=2&size=0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error",
                        is("searchItem.size: must be greater than or equal to 1")))
                .andExpect(jsonPath("$.description", is("Validation Exception")));

        verifyNoInteractions(service);
    }

    @Test
    void searchItemWithoutSearchWithStatusInternalServerError() throws Exception {
        mvc.perform(get("/items/search?from=2&size=2"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error",
                        is("Required request parameter 'text' for method parameter type String is not present")))
                .andExpect(jsonPath("$.description", is("Unknown")));

        verifyNoInteractions(service);
    }

    @Test
    void searchItemWithStatusNotFound() throws Exception {
        when(service.search(anyString(), anyInt(), anyInt()))
                .thenThrow(new NotFoundException("Искомая вещь не найдена!"));

        mvc.perform(get("/items/search?text=Поиск&from=2&size=2"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Искомая вещь не найдена!")))
                .andExpect(jsonPath("$.description", is("Not found exception")));

        verify(service, times(1)).search("Поиск", 2, 2);
    }

    @Test
    void addItemWithStatusOk() throws Exception {
        when(service.add(any(), anyLong()))
                .thenReturn(dtoShort);

        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(dto1))
                        .characterEncoding(UTF_8)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(dtoShort.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(dtoShort.getDescription())));

        verify(service, times(1)).add(dto1, 1L);
    }

    @Test
    void addItemWithWrongUserAndStatusNotFound() throws Exception {
        when(service.add(any(), anyLong()))
                .thenThrow(new NotFoundException("Пользователь не найден!"));

        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(dto1))
                        .characterEncoding(UTF_8)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 29L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Пользователь не найден!")))
                .andExpect(jsonPath("$.description", is("Not found exception")));

        verify(service, times(1)).add(dto1, 29L);
    }

    @Test
    void addItemWithoutNameAndStatusBadRequest() throws Exception {
        ItemDto noName = new ItemDto(1L,
                null,
                "description",
                true,
                2L,
                null,
                null,
                null);

        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(noName))
                        .characterEncoding(UTF_8)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("addItem.itemDto.name: must not be blank")))
                .andExpect(jsonPath("$.description", is("Validation Exception")));

        verifyNoInteractions(service);
    }

    @Test
    void addItemWithoutDescriptionAndStatusBadRequest() throws Exception {
        ItemDto noDescription = new ItemDto(1L,
                "name",
                null,
                true,
                2L,
                null,
                null,
                null);

        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(noDescription))
                        .characterEncoding(UTF_8)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("addItem.itemDto.description: must not be blank")))
                .andExpect(jsonPath("$.description", is("Validation Exception")));

        verifyNoInteractions(service);
    }

    @Test
    void addItemWithoutAvailableAndStatusBadRequest() throws Exception {
        ItemDto noAvailable = new ItemDto(1L,
                "name",
                "description",
                null,
                2L,
                null,
                null,
                null);

        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(noAvailable))
                        .characterEncoding(UTF_8)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("addItem.itemDto.available: must not be null")))
                .andExpect(jsonPath("$.description", is("Validation Exception")));

        verifyNoInteractions(service);
    }

    @Test
    void updateItemWithStatusOk() throws Exception {
        when(service.update(any(), anyLong()))
                .thenReturn(dtoShort);

        mvc.perform(patch("/items/2")
                        .content(mapper.writeValueAsString(dto1))
                        .characterEncoding(UTF_8)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(dtoShort.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(dtoShort.getName())))
                .andExpect(jsonPath("$.description", is(dtoShort.getDescription())));

        verify(service, times(1)).update(dto1, 1L);
    }

    @Test
    void updateItemWithStatusNotFound() throws Exception {
        when(service.update(any(), anyLong()))
                .thenThrow(new NotFoundException("Тестовое исключение"));

        mvc.perform(patch("/items/2")
                        .content(mapper.writeValueAsString(dto1))
                        .characterEncoding(UTF_8)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Тестовое исключение")))
                .andExpect(jsonPath("$.description", is("Not found exception")));

        verify(service, times(1)).update(dto1, 1L);
    }

    @Test
    void deleteItemWithStatusOk() throws Exception {
        mvc.perform(delete("/items/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());

        verify(service, times(1)).delete(1L, 1L);
    }

    @Test
    void deleteItemWithStatusConflict() throws Exception {
        doThrow(new ConflictException("Конфликт!"))
                .when(service).delete(anyLong(), anyLong());

        mvc.perform(delete("/items/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", is("Конфликт!")))
                .andExpect(jsonPath("$.description", is("Conflict Exception")));

        verify(service, times(1)).delete(1L, 1L);
    }

    @Test
    void addCommentWithStatusOk() throws Exception {
        when(service.addComment(any(), anyLong(), anyLong()))
                .thenReturn(commentDto);

        mvc.perform(post("/items/2/comment")
                        .content(mapper.writeValueAsString(commentDto))
                        .characterEncoding(UTF_8)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(commentDto.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(commentDto.getText())));

        verify(service, times(1)).addComment(commentDto, 2L, 1L);
    }

    @Test
    void addCommentWithoutTextWithStatusBadRequest() throws Exception {
        CommentDto noText = new CommentDto(1L, null, "author", null);

        mvc.perform(post("/items/2/comment")
                        .content(mapper.writeValueAsString(noText))
                        .characterEncoding(UTF_8)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description", is("Validation Exception")));

        verifyNoInteractions(service);
    }

    @Test
    void addCommentWithStatusNotFound() throws Exception {
        when(service.addComment(any(), anyLong(), anyLong()))
                .thenThrow(new NotFoundException("Вещь не найдена!"));

        mvc.perform(post("/items/2/comment")
                        .content(mapper.writeValueAsString(commentDto))
                        .characterEncoding(UTF_8)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Вещь не найдена!")))
                .andExpect(jsonPath("$.description", is("Not found exception")));

        verify(service, times(1)).addComment(commentDto, 2L, 1L);
    }

    @Test
    void addCommentWithStatusBadRequest() throws Exception {
        when(service.addComment(any(), anyLong(), anyLong()))
                .thenThrow(new BadRequestException("Пользователь не может оставить отзыв об этой вещи"));

        mvc.perform(post("/items/2/comment")
                        .content(mapper.writeValueAsString(commentDto))
                        .characterEncoding(UTF_8)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Пользователь не может оставить отзыв об этой вещи")))
                .andExpect(jsonPath("$.description", is("Bad request exception")));

        verify(service, times(1)).addComment(commentDto, 2L, 1L);
    }
}