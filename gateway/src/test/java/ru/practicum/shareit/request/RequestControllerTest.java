package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.RequestDto;

import java.time.LocalDateTime;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RequestController.class)
@AutoConfigureWebMvc
class RequestControllerTest {

    @Autowired
    ObjectMapper mapper;

    @MockBean
    RequestClient client;

    @Autowired
    private MockMvc mvc;

    private final LocalDateTime timestamp1 = LocalDateTime.of(2022, 11, 23, 10, 30);

    @Test
    void addRequestWithoutDescriptionAndStatusBadRequest() throws Exception {
        RequestDto noDescription = new RequestDto(1L, null, timestamp1);

        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(noDescription))
                        .characterEncoding(UTF_8)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error",
                        containsString("must not be blank")))
                .andExpect(jsonPath("$.description", is("Validation Exception")));

        verifyNoInteractions(client);
    }

    @Test
    void getAllWithWrongFromWithStatusBadRequest() throws Exception {
        mvc.perform(get("/requests/all?from=-2&size=2")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("getAll.from: must be greater than or equal to 0")))
                .andExpect(jsonPath("$.description", is("Validation Exception")));

        verifyNoInteractions(client);
    }

    @Test
    void getAllWithWrongSizeWithStatusBadRequest() throws Exception {
        mvc.perform(get("/requests/all?from=2&size=0")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("getAll.size: must be greater than or equal to 1")))
                .andExpect(jsonPath("$.description", is("Validation Exception")));

        verifyNoInteractions(client);
    }
}