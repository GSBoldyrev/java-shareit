package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDtoIncome;

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

@WebMvcTest(controllers = BookingController.class)
@AutoConfigureWebMvc
class BookingControllerTest {

    @Autowired
    ObjectMapper mapper;

    @MockBean
    BookingClient client;

    @Autowired
    private MockMvc mvc;

    private final LocalDateTime start = LocalDateTime.of(2035, 1, 1, 1, 1, 1);
    private final LocalDateTime end = LocalDateTime.of(2045, 1, 1, 1, 1, 1);
    private final LocalDateTime past = LocalDateTime.of(2005, 1, 1, 1, 1, 1);

    @Test
    void addBookingWithWrongStartWithStatusBadRequest() throws Exception {
        BookingDtoIncome noStart = new BookingDtoIncome(past, end, 1L);

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(noStart))
                        .characterEncoding(UTF_8)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("must be a future date")))
                .andExpect(jsonPath("$.description", is("Validation Exception")));

        verifyNoInteractions(client);
    }

    @Test
    void addBookingWithWrongEndWithStatusBadRequest() throws Exception {
        BookingDtoIncome noEnd = new BookingDtoIncome(start, past, 1L);

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(noEnd))
                        .characterEncoding(UTF_8)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("must be a future date")))
                .andExpect(jsonPath("$.description", is("Validation Exception")));

        verifyNoInteractions(client);
    }

    @Test
    void addBookingWithWrongItemIdWithStatusBadRequest() throws Exception {
        BookingDtoIncome noItem = new BookingDtoIncome(start, end, null);

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(noItem))
                        .characterEncoding(UTF_8)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("must not be null")))
                .andExpect(jsonPath("$.description", is("Validation Exception")));

        verifyNoInteractions(client);
    }

    @Test
    void getBookingsForUserWithWrongFromWithStatusBadRequest() throws Exception {
        mvc.perform(get("/bookings?state=WAITING&from=-2&size=2")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error",
                        is("getBookingsForUser.from: must be greater than or equal to 0")))
                .andExpect(jsonPath("$.description", is("Validation Exception")));

        verifyNoInteractions(client);
    }

    @Test
    void getBookingsForUserWithWrongSizeWithStatusBadRequest() throws Exception {
        mvc.perform(get("/bookings?state=WAITING&from=2&size=0")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error",
                        is("getBookingsForUser.size: must be greater than or equal to 1")))
                .andExpect(jsonPath("$.description", is("Validation Exception")));

        verifyNoInteractions(client);
    }

    @Test
    void getBookingsForOwnerWithWrongFromWithStatusBadRequest() throws Exception {
        mvc.perform(get("/bookings/owner?state=WAITING&from=-2&size=2")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error",
                        is("getBookingsForOwner.from: must be greater than or equal to 0")))
                .andExpect(jsonPath("$.description", is("Validation Exception")));

        verifyNoInteractions(client);
    }

    @Test
    void getBookingsForOwnerWithWrongSizeWithStatusBadRequest() throws Exception {
        mvc.perform(get("/bookings/owner?state=WAITING&from=2&size=0")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error",
                        is("getBookingsForOwner.size: must be greater than or equal to 1")))
                .andExpect(jsonPath("$.description", is("Validation Exception")));

        verifyNoInteractions(client);
    }
}