package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

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
class UserServiceIntegrationTest {

    private final EntityManager em;
    private final UserService service;
    private final UserDto dto = new UserDto(null, "Petr", "petr@yandex.ru");

    @Test
    void add() {
        service.add(dto);

        TypedQuery<User> query = em.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class);
        User user = query
                .setParameter("email", dto.getEmail())
                .getSingleResult();

        assertThat(user.getId(), equalTo(4L));
        assertThat(user.getName(), equalTo(dto.getName()));
        assertThat(user.getEmail(), equalTo(dto.getEmail()));
    }

    @Test
    void updateName() {
        service.update(new UserDto(null, "Gamma", null), 1L);

        TypedQuery<User> query = em.createQuery("SELECT u FROM User u WHERE u.id = :id", User.class);
        User user = query
                .setParameter("id", 1L)
                .getSingleResult();

        assertThat(user.getName(), equalTo("Gamma"));
        assertThat(user.getEmail(), equalTo("alfa@yandex.ru"));
    }

    @Test
    void updateEmail() {
        service.update(new UserDto(null, null, "gamma@yandex.ru"), 1L);

        TypedQuery<User> query = em.createQuery("SELECT u FROM User u WHERE u.id = :id", User.class);
        User user = query
                .setParameter("id", 1L)
                .getSingleResult();

        assertThat(user.getName(), equalTo("Alfa"));
        assertThat(user.getEmail(), equalTo("gamma@yandex.ru"));
    }

    @Test
    void updateFail() {
        NotFoundException e = assertThrows(NotFoundException.class,
                () -> service.update(new UserDto(null, null, "gamma@yandex.ru"), 47L));
        assertThat(e.getMessage(), equalTo("Пользователь по ID 47 не найден!"));
    }

    @Test
    void delete() {
        service.delete(2L);
        List<UserDto> dtoList = service.getAll();

        assertThat(dtoList.size(), equalTo(2));
    }

    @Test
    void getById() {
        UserDto user = service.getById(2L);

        assertThat(user.getName(), equalTo("Beta"));
        assertThat(user.getEmail(), equalTo("beta@yandex.ru"));
    }

    @Test
    void getByIdFail() {
        NotFoundException e = assertThrows(NotFoundException.class,
                () -> service.getById(94L));
        assertThat(e.getMessage(), equalTo("Пользователь по ID 94 не найден!"));
    }

    @Test
    void getAll() {
        List<UserDto> dtoList = service.getAll();

        assertThat(dtoList.size(), equalTo(3));
        assertThat(dtoList.get(0).getName(), equalTo("Alfa"));
        assertThat(dtoList.get(2).getName(), equalTo("Delta"));
    }
}