package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserDto;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureWebMvc
class UserControllerTest {

    @Autowired
    ObjectMapper mapper;

    @MockBean
    UserClient client;

    @Autowired
    private MockMvc mvc;

    @Test
    void createUserWithoutNameWithStatusBadRequest() throws Exception {
        UserDto noName = new UserDto(3L, null, "noName@yandex.ru");

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(noName))
                        .characterEncoding(UTF_8)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("must not be null")))
                .andExpect(jsonPath("$.description", is("Validation Exception")));

        verifyNoInteractions(client);
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
                .andExpect(jsonPath("$.error", containsString("must not be null")))
                .andExpect(jsonPath("$.description", is("Validation Exception")));

        verifyNoInteractions(client);
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
                        containsString("must be a well-formed email address")))
                .andExpect(jsonPath("$.description", is("Validation Exception")));

        verifyNoInteractions(client);
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
                .andExpect(jsonPath("$.error", containsString("must be null")))
                .andExpect(jsonPath("$.description", is("Validation Exception")));

        verifyNoInteractions(client);
    }
}