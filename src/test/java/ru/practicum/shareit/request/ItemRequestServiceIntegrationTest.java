package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoFull;
import ru.practicum.shareit.request.model.ItemRequest;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Transactional
class ItemRequestServiceIntegrationTest {

    private final EntityManager em;
    private final ItemRequestService service;
    private final ItemRequestDto request = new ItemRequestDto(null, "Нужен инструмент", null);

    @Test
    void add() {
        service.add(request, 1L);

        TypedQuery<ItemRequest> query = em
                .createQuery("SELECT i FROM ItemRequest i WHERE i.description = :description", ItemRequest.class);
        ItemRequest checkedRequest = query
                .setParameter("description", request.getDescription())
                .getSingleResult();

        assertThat(checkedRequest.getId(), equalTo(3L));
        assertThat(checkedRequest.getDescription(), equalTo(request.getDescription()));
    }

    @Test
    void addFail() {
        NotFoundException e = assertThrows(NotFoundException.class,
                () -> service.add(request, 117L));
        assertThat(e.getMessage(), equalTo("Пользователь по ID 117 не найден"));
    }

    @Test
    void getById() {
        ItemRequestDtoFull fullRequest = service.getById(3L, 1L);

        assertThat(fullRequest.getDescription(), equalTo("Хочу отвертку"));
        assertThat(fullRequest.getItems().size(), equalTo(1));
        assertThat(fullRequest.getItems().get(0).getName(), equalTo("Отвертка"));
    }

    @Test
    void getByIdFailWrongUser() {
        NotFoundException e = assertThrows(NotFoundException.class,
                () -> service.getById(456L, 1L));
        assertThat(e.getMessage(), equalTo("Пользователь по ID 456 не найден"));
    }

    @Test
    void getByIdFailWrongRequest() {
        NotFoundException e = assertThrows(NotFoundException.class,
                () -> service.getById(2L, 117L));
        assertThat(e.getMessage(), equalTo("Запрос 117 не найден!"));
    }

    @Test
    void getForAuthor() {
        List<ItemRequestDtoFull> requests = service.getForAuthor(1L);

        assertThat(requests.size(), equalTo(1));
        assertThat(requests.get(0).getDescription(), equalTo("Хочу отвертку"));
    }

    @Test
    void getForAuthorFail() {
        NotFoundException e = assertThrows(NotFoundException.class,
                () -> service.getForAuthor(27L));

        assertThat(e.getMessage(), equalTo("Пользователь по ID 27 не найден"));
    }

    @Test
    void getAll() {
        List<ItemRequestDtoFull> requests = service.getAll(1L, 0, 2);

        assertThat(requests.size(), equalTo(1));
        assertThat(requests.get(0).getDescription(), equalTo("Хочу дрель"));
    }
}