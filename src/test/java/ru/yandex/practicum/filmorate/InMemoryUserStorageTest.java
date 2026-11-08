package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryUserStorageTest {
    private InMemoryUserStorage storage;

    @BeforeEach
    void setUp() {
        storage = new InMemoryUserStorage();
    }

    @Test
    void addShouldAssignIncrementingIds() {
        assertEquals(1L, storage.add(user("a")).getId());
        assertEquals(2L, storage.add(user("b")).getId());
    }

    @Test
    void getByIdShouldReturnStored() {
        User saved = storage.add(user("a"));

        assertEquals(saved, storage.getById(saved.getId()));
    }

    @Test
    void getByIdShouldThrowNotFoundExceptionWhenNotFound() {
        assertThrows(NotFoundException.class, () -> storage.getById(999L));
    }

    @Test
    void findAllShouldReturnAll() {
        storage.add(user("a"));
        storage.add(user("b"));

        assertEquals(2, storage.findAll().size());
    }

    @Test
    void findAllShouldReturnCopy() {
        storage.add(user("a"));
        List<User> users = storage.findAll();
        users.clear();

        assertEquals(1, storage.findAll().size());
    }

    @Test
    void updateShouldReplaceExistingUser() {
        User saved = storage.add(user("a"));
        saved.setName("B");

        assertEquals("B", storage.update(saved).getName());
        assertEquals("B", storage.getById(saved.getId()).getName());
    }

    @Test
    void updateShouldThrowWhenUnknownUser() {
        User u = user("a");
        u.setId(999L);

        assertThrows(NotFoundException.class, () -> storage.update(u));
    }

    @Test
    void updateShouldThrowExceptionWhenNullId() {
        User u = user("a");
        u.setId(null);

        assertThrows(NotFoundException.class, () -> storage.update(u));
    }

    @Test
    void deleteShouldRemoveUser() {
        User saved = storage.add(user("a"));
        storage.delete(saved.getId());

        assertThrows(NotFoundException.class, () -> storage.getById(saved.getId()));
    }

    @Test
    void deleteUnknownShouldNotThrow() {
        assertDoesNotThrow(() -> storage.delete(999L));
    }

    private User user(String login) {
        User u = new User();
        u.setEmail(login + "@example.com");
        u.setLogin(login);
        u.setName(login);
        u.setBirthday(LocalDate.of(1990, 1, 1));

        return u;
    }
}