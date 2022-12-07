package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.error.exception.BadRequestException;
import ru.practicum.shareit.error.exception.ConflictException;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoShort;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Transactional
class ItemServiceIntegrationTest {

    private final EntityManager em;
    private final ItemService service;
    private final ItemDto item = new ItemDto(null,
            "Кувалда",
            "Огромная!",
            true,
            null,
            null,
            null,
            null);
    private final CommentDto comment = new CommentDto(null,
            "Отказали!",
            null,
            null);

    @Test
    void getByIdNoBookings() {
        ItemDto item = service.getById(1L, 2L);

        assertThat(item.getName(), equalTo("Пила"));
        assertThat(item.getLastBooking(), nullValue());
        assertThat(item.getNextBooking(), nullValue());
    }

    @Test
    void getByIdWithBookings() {
        ItemDto item = service.getById(1L, 1L);

        assertThat(item.getName(), equalTo("Пила"));
        assertThat(item.getLastBooking(), notNullValue());
        assertThat(item.getNextBooking(), notNullValue());
    }

    @Test
    void getByIdWithComments() {
        ItemDto item = service.getById(3L, 3L);

        assertThat(item.getName(), equalTo("Отвертка"));
        assertThat(item.getComments().size(), equalTo(1));
        assertThat(item.getComments().get(0).getText(), equalTo("Понравилось!"));
    }

    @Test
    void getByIdFail() {
        NotFoundException e = assertThrows(NotFoundException.class,
                () -> service.getById(94L, 1L));
        assertThat(e.getMessage(), equalTo("Вещь 94 не найдена!"));
    }

    @Test
    void getAll() {
        List<ItemDto> items = service.getAll(1L, 0, 2);
        assertThat(items.size(), equalTo(2));
        assertThat(items.get(0).getName(), equalTo("Пила"));
        assertThat(items.get(1).getName(), equalTo("Молоток"));
    }

    @Test
    void getAllFromSecond() {
        List<ItemDto> items = service.getAll(3L, 1, 1);
        assertThat(items.size(), equalTo(1));
        assertThat(items.get(0).getName(), equalTo("Дрель"));
    }

    @Test
    void add() {
        service.add(item, 1L);

        TypedQuery<Item> query = em.createQuery("SELECT i FROM Item i WHERE i.name = :name", Item.class);
        Item checkedItem = query
                .setParameter("name", item.getName())
                .getSingleResult();

        assertThat(checkedItem.getId(), equalTo(6L));
        assertThat(checkedItem.getName(), equalTo(item.getName()));
        assertThat(checkedItem.getDescription(), equalTo(item.getDescription()));
    }

    @Test
    void addFail() {
        NotFoundException e = assertThrows(NotFoundException.class,
                () -> service.add(item, 117L));
        assertThat(e.getMessage(), equalTo("Пользователь по ID 117 не найден"));
    }

    @Test
    void updateName() {
        item.setId(1L);
        item.setAvailable(null);
        item.setDescription(null);

        service.update(item, 1L);

        TypedQuery<Item> query = em.createQuery("SELECT i FROM Item i WHERE i.id = :id", Item.class);
        Item checkedItem = query
                .setParameter("id", 1L)
                .getSingleResult();

        assertThat(checkedItem.getName(), equalTo("Кувалда"));
        assertThat(checkedItem.getDescription(), equalTo("Очень острая"));
        assertThat(checkedItem.getAvailable(), equalTo(true));
    }

    @Test
    void updateDescription() {
        item.setId(1L);
        item.setAvailable(null);
        item.setName(null);

        service.update(item, 1L);

        TypedQuery<Item> query = em.createQuery("SELECT i FROM Item i WHERE i.id = :id", Item.class);
        Item checkedItem = query
                .setParameter("id", 1L)
                .getSingleResult();

        assertThat(checkedItem.getName(), equalTo("Пила"));
        assertThat(checkedItem.getDescription(), equalTo("Огромная!"));
        assertThat(checkedItem.getAvailable(), equalTo(true));
    }

    @Test
    void updateAvailable() {
        item.setId(1L);
        item.setAvailable(false);
        item.setDescription(null);
        item.setName(null);

        service.update(item, 1L);

        TypedQuery<Item> query = em.createQuery("SELECT i FROM Item i WHERE i.id = :id", Item.class);
        Item checkedItem = query
                .setParameter("id", 1L)
                .getSingleResult();

        assertThat(checkedItem.getName(), equalTo("Пила"));
        assertThat(checkedItem.getDescription(), equalTo("Очень острая"));
        assertThat(checkedItem.getAvailable(), equalTo(false));
    }

    @Test
    void updateFailWrongUser() {
        item.setId(1L);
        NotFoundException e = assertThrows(NotFoundException.class,
                () -> service.update(item, 117L));
        assertThat(e.getMessage(), equalTo("Пользователь по ID 117 не найден"));
    }

    @Test
    void updateFailWrongOwner() {
        item.setId(1L);
        NotFoundException e = assertThrows(NotFoundException.class,
                () -> service.update(item, 2L));
        assertThat(e.getMessage(), equalTo("Вещь 1 не найдена!"));
    }

    @Test
    void updateFailWrongItem() {
        item.setId(124L);
        NotFoundException e = assertThrows(NotFoundException.class,
                () -> service.update(item, 1L));
        assertThat(e.getMessage(), equalTo("Вещь 124 не найдена!"));
    }

    @Test
    void delete() {
        service.delete(1L, 1L);
        List<ItemDto> items = service.getAll(1L, 0, 2);

        assertThat(items.size(), equalTo(1));
    }

    @Test
    void deleteFail() {
        ConflictException e = assertThrows(ConflictException.class, () -> service.delete(1L, 2L));

        assertThat(e.getMessage(), equalTo("Это ведь не ваша вещь, чтоб ее удалять!"));
    }

    @Test
    void search() {
        List<ItemDtoShort> searched = service.search("МолОТ", 0, 5);

        assertThat(searched.size(), equalTo(2));
        assertThat(searched.get(0).getDescription(), equalTo("Огромный"));
        assertThat(searched.get(1).getDescription(), equalTo("Молоток в комплекте"));
    }

    @Test
    void searchFail() {
        NotFoundException e = assertThrows(NotFoundException.class, () -> service.search("ббб", 2, 2));

        assertThat(e.getMessage(), equalTo("Искомая вещь не найдена!"));
    }

    @Test
    void addComment() {
        service.addComment(comment, 1L, 2L);

        TypedQuery<Comment> query = em.createQuery("SELECT c FROM Comment c WHERE c.text = :text", Comment.class);
        Comment checkedComment = query
                .setParameter("text", comment.getText())
                .getSingleResult();

        assertThat(checkedComment.getText(), equalTo("Отказали!"));
        assertThat(checkedComment.getAuthor().getId(), equalTo(2L));
        assertThat(checkedComment.getItem().getId(), equalTo(1L));
    }

    @Test
    void addCommentFailWrongItem() {
        NotFoundException e = assertThrows(NotFoundException.class,
                () -> service.addComment(comment, 24L, 2L));

        assertThat(e.getMessage(), equalTo("Вещь по ID 24 не найдена!"));
    }

    @Test
    void addCommentFailWrongUser() {
        NotFoundException e = assertThrows(NotFoundException.class,
                () -> service.addComment(comment, 2L, 25L));

        assertThat(e.getMessage(), equalTo("Пользователь по ID 25 не найден!"));
    }

    @Test
    void addCommentFailWrongBooking() {
        BadRequestException e = assertThrows(BadRequestException.class,
                () -> service.addComment(comment, 2L, 1L));

        assertThat(e.getMessage(), equalTo("Пользователь 1 не может оставить отзыв о вещи 2"));
    }
}