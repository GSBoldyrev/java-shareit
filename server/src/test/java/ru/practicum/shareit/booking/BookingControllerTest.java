package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDtoIncome;
import ru.practicum.shareit.booking.dto.BookingDtoOutcome;
import ru.practicum.shareit.error.exception.BadRequestException;
import ru.practicum.shareit.error.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.booking.model.Status.APPROVED;
import static ru.practicum.shareit.booking.model.Status.WAITING;

@WebMvcTest(controllers = BookingController.class)
@AutoConfigureWebMvc
class BookingControllerTest {

    @Autowired
    ObjectMapper mapper;

    @MockBean
    BookingService service;

    @Autowired
    private MockMvc mvc;

    private final LocalDateTime start = LocalDateTime.of(2035, 1, 1, 1, 1, 1);
    private final LocalDateTime end = LocalDateTime.of(2045, 1, 1, 1, 1, 1);
    private final BookingDtoIncome dtoIn = new BookingDtoIncome(start, end, 1L);
    private final BookingDtoOutcome dtoOut1 = new BookingDtoOutcome(2L,
            null,
            null,
            null,
            null,
            WAITING);
    private final BookingDtoOutcome dtoOut2 = new BookingDtoOutcome(3L,
            null,
            null,
            null,
            null,
            APPROVED);

    @Test
    void addBookingWithStatusOk() throws Exception {
        when(service.add(any(), anyLong()))
                .thenReturn(dtoOut1);

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(dtoIn))
                        .characterEncoding(UTF_8)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(dtoOut1.getId()), Long.class))
                .andExpect(jsonPath("$.status", is(dtoOut1.getStatus().name())));

        verify(service, times(1)).add(dtoIn, 1L);
    }







    @Test
    void addBookingWithStatusNotFound() throws Exception {
        when(service.add(any(), anyLong()))
                .thenThrow(new NotFoundException("Тест!"));

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(dtoIn))
                        .characterEncoding(UTF_8)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Тест!")))
                .andExpect(jsonPath("$.description", is("Not found exception")));

        verify(service, times(1)).add(dtoIn, 1L);
    }

    @Test
    void addBookingWithStatusBadRequest() throws Exception {
        when(service.add(any(), anyLong()))
                .thenThrow(new BadRequestException("Тест!"));

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(dtoIn))
                        .characterEncoding(UTF_8)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Тест!")))
                .andExpect(jsonPath("$.description", is("Bad request exception")));

        verify(service, times(1)).add(dtoIn, 1L);
    }

    @Test
    void approveBookingWithStatusOk() throws Exception {
        when(service.approve(anyLong(), anyLong(), anyBoolean()))
                .thenReturn(dtoOut1);

        mvc.perform(patch("/bookings/2?approved=true")
                        .characterEncoding(UTF_8)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(dtoOut1.getId()), Long.class))
                .andExpect(jsonPath("$.status", is(dtoOut1.getStatus().name())));

        verify(service, times(1)).approve(1L, 2L, true);
    }

    @Test
    void approveBookingWithStatusBadRequest() throws Exception {
        when(service.approve(anyLong(), anyLong(), anyBoolean()))
                .thenThrow(new BadRequestException("Тест!"));

        mvc.perform(patch("/bookings/2?approved=true")
                        .characterEncoding(UTF_8)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Тест!")))
                .andExpect(jsonPath("$.description", is("Bad request exception")));

        verify(service, times(1)).approve(1L, 2L, true);
    }

    @Test
    void approveBookingWithStatusNotFound() throws Exception {
        when(service.approve(anyLong(), anyLong(), anyBoolean()))
                .thenThrow(new NotFoundException("Тест!"));

        mvc.perform(patch("/bookings/2?approved=true")
                        .characterEncoding(UTF_8)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Тест!")))
                .andExpect(jsonPath("$.description", is("Not found exception")));

        verify(service, times(1)).approve(1L, 2L, true);
    }

    @Test
    void getBookingWithStatusOk() throws Exception {
        when(service.get(anyLong(), anyLong()))
                .thenReturn(dtoOut1);

        mvc.perform(get("/bookings/2")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(dtoOut1.getId()), Long.class))
                .andExpect(jsonPath("$.status", is(dtoOut1.getStatus().name())));

        verify(service, times(1)).get(1L, 2L);
    }

    @Test
    void getBookingWithStatusNotFound() throws Exception {
        when(service.get(anyLong(), anyLong()))
                .thenThrow(new NotFoundException("Тест!"));

        mvc.perform(get("/bookings/2")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Тест!")))
                .andExpect(jsonPath("$.description", is("Not found exception")));

        verify(service, times(1)).get(1L, 2L);
    }

    @Test
    void getBookingsForUserWithStatusOk() throws Exception {
        when(service.getForUser(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(dtoOut1, dtoOut2));

        mvc.perform(get("/bookings?state=WAITING&from=2&size=2")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].status", is(dtoOut1.getStatus().name())))
                .andExpect(jsonPath("$[1].status", is(dtoOut2.getStatus().name())));

        verify(service, times(1)).getForUser(1L, "WAITING", 2, 2);

    }





    @Test
    void getBookingsForUserWithStatusBadRequest() throws Exception {
        when(service.getForUser(anyLong(), anyString(), anyInt(), anyInt()))
                .thenThrow(new BadRequestException("Тест!"));

        mvc.perform(get("/bookings?state=WAITING&from=2&size=2")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Тест!")))
                .andExpect(jsonPath("$.description", is("Bad request exception")));

        verify(service, times(1)).getForUser(1L, "WAITING", 2, 2);
    }

    @Test
    void getBookingsForUserWithStatusNotFound() throws Exception {
        when(service.getForUser(anyLong(), anyString(), anyInt(), anyInt()))
                .thenThrow(new NotFoundException("Тест!"));

        mvc.perform(get("/bookings?state=WAITING&from=2&size=2")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Тест!")))
                .andExpect(jsonPath("$.description", is("Not found exception")));

        verify(service, times(1)).getForUser(1L, "WAITING", 2, 2);
    }

    @Test
    void getBookingsForOwnerWithStatusOk() throws Exception {
        when(service.getForOwner(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(dtoOut1, dtoOut2));

        mvc.perform(get("/bookings/owner?state=WAITING&from=2&size=2")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].status", is(dtoOut1.getStatus().name())))
                .andExpect(jsonPath("$[1].status", is(dtoOut2.getStatus().name())));

        verify(service, times(1)).getForOwner(1L, "WAITING", 2, 2);
    }





    @Test
    void getBookingsForOwnerWithStatusBadRequest() throws Exception {
        when(service.getForOwner(anyLong(), anyString(), anyInt(), anyInt()))
                .thenThrow(new BadRequestException("Тест!"));

        mvc.perform(get("/bookings/owner?state=WAITING&from=2&size=2")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Тест!")))
                .andExpect(jsonPath("$.description", is("Bad request exception")));

        verify(service, times(1)).getForOwner(1L, "WAITING", 2, 2);
    }

    @Test
    void getBookingsForOwnerWithStatusNotFound() throws Exception {
        when(service.getForOwner(anyLong(), anyString(), anyInt(), anyInt()))
                .thenThrow(new NotFoundException("Тест!"));

        mvc.perform(get("/bookings/owner?state=WAITING&from=2&size=2")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Тест!")))
                .andExpect(jsonPath("$.description", is("Not found exception")));

        verify(service, times(1)).getForOwner(1L, "WAITING", 2, 2);
    }
}