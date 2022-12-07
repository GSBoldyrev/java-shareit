package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
@AutoConfigureWebMvc
class ItemControllerTest {

    @Autowired
    ObjectMapper mapper;

    @MockBean
    ItemClient client;

    @Autowired
    private MockMvc mvc;

    @Test
    void getItemsWithWrongFromWithStatusBadRequest() throws Exception {
        mvc.perform(get("/items?from=-2&size=2")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("getItems.from: must be greater than or equal to 0")))
                .andExpect(jsonPath("$.description", is("Validation Exception")));

        verifyNoInteractions(client);
    }

    @Test
    void getItemsWithWrongSizeWithStatusBadRequest() throws Exception {
        mvc.perform(get("/items?from=2&size=0")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("getItems.size: must be greater than or equal to 1")))
                .andExpect(jsonPath("$.description", is("Validation Exception")));

        verifyNoInteractions(client);
    }

    @Test
    void searchItemWithWrongFromWithStatusBadRequest() throws Exception {
        mvc.perform(get("/items/search?text=Поиск&from=-2&size=2")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error",
                        containsString("must be greater than or equal to 0")))
                .andExpect(jsonPath("$.description", is("Validation Exception")));

        verifyNoInteractions(client);
    }

    @Test
    void searchItemWithWrongSizeWithStatusBadRequest() throws Exception {
        mvc.perform(get("/items/search?text=Поиск&from=2&size=0")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error",
                        containsString("must be greater than or equal to 1")))
                .andExpect(jsonPath("$.description", is("Validation Exception")));

        verifyNoInteractions(client);
    }

    @Test
    void searchItemWithoutSearchWithStatusInternalServerError() throws Exception {
        mvc.perform(get("/items/search?from=2&size=2")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error",
                        is("Required request parameter 'text' for method parameter type String is not present")))
                .andExpect(jsonPath("$.description", is("Unknown")));

        verifyNoInteractions(client);
    }

    @Test
    void addItemWithoutNameAndStatusBadRequest() throws Exception {
        ItemDto noName = new ItemDto(1L,
                null,
                "description",
                true,
                2L);

        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(noName))
                        .characterEncoding(UTF_8)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("addItem.itemDto.name: must not be blank")))
                .andExpect(jsonPath("$.description", is("Validation Exception")));

        verifyNoInteractions(client);
    }

    @Test
    void addItemWithoutDescriptionAndStatusBadRequest() throws Exception {
        ItemDto noDescription = new ItemDto(1L,
                "name",
                null,
                true,
                2L);

        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(noDescription))
                        .characterEncoding(UTF_8)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("addItem.itemDto.description: must not be blank")))
                .andExpect(jsonPath("$.description", is("Validation Exception")));

        verifyNoInteractions(client);
    }

    @Test
    void addItemWithoutAvailableAndStatusBadRequest() throws Exception {
        ItemDto noAvailable = new ItemDto(1L,
                "name",
                "description",
                null,
                2L);

        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(noAvailable))
                        .characterEncoding(UTF_8)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("addItem.itemDto.available: must not be null")))
                .andExpect(jsonPath("$.description", is("Validation Exception")));

        verifyNoInteractions(client);
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

        verifyNoInteractions(client);
    }
}