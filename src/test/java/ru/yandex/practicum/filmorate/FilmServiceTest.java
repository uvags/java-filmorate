package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class FilmServiceTest {
    private FilmService service;
    private InMemoryUserStorage userStorage;

    @BeforeEach
    void setUp() {
        userStorage = new InMemoryUserStorage();
        service = new FilmService(new InMemoryFilmStorage(), userStorage);
    }

    private Film film(String name) {
        Film f = new Film();
        f.setName(name);
        f.setDescription("description");
        f.setReleaseDate(LocalDate.of(2000, 1, 1));
        f.setDuration(100);

        return f;
    }

    private User user(String login) {
        User u = new User();
        u.setEmail(login + "@example.com");
        u.setLogin(login);
        u.setName(login);
        u.setBirthday(LocalDate.of(1990, 1, 1));

        return userStorage.add(u);
    }

    @Test
    void createShouldAssignId() {
        Film saved = service.create(film("A"));
        assertNotNull(saved.getId());

        assertEquals(saved, service.getById(saved.getId()));
    }

    @Test
    void createShouldRejectInvalidFilm() {
        Film invalid = film("A");
        invalid.setName("");

        assertThrows(ValidationException.class, () -> service.create(invalid));
        assertTrue(service.findAll().isEmpty());
    }

    @Test
    void getByIdShouldThrowWhenNotFound() {
        assertThrows(NotFoundException.class, () -> service.getById(999L));
    }

    @Test
    void findAllShouldReturnAllFilms() {
        service.create(film("A"));
        service.create(film("B"));

        assertEquals(2, service.findAll().size());
    }

    @Test
    void updateShouldUpdateFilm() {
        Film saved = service.create(film("A"));
        saved.setName("B");

        assertEquals("B", service.update(saved).getName());
    }

    @Test
    void updateShouldThrowForUnknownFilm() {
        Film f = film("A");
        f.setId(999L);

        assertThrows(NotFoundException.class, () -> service.update(f));
    }

    @Test
    void updateShouldRejectInvalidFilm() {
        Film saved = service.create(film("A"));
        saved.setName("");

        assertThrows(ValidationException.class, () -> service.update(saved));
    }

    @Test
    void addLikeShouldAddLike() {
        Film film = service.create(film("A"));
        User u = user("u1");
        service.addLike(film.getId(), u.getId());

        assertEquals(1, service.getById(film.getId()).getLikes().size());
    }

    @Test
    void addLikeShouldNotDuplicate() {
        Film film = service.create(film("A"));
        User u = user("u1");
        service.addLike(film.getId(), u.getId());
        service.addLike(film.getId(), u.getId());

        assertEquals(1, service.getById(film.getId()).getLikes().size());
    }

    @Test
    void addLikeShouldThrowForUnknownFilm() {
        User u = user("u1");

        assertThrows(NotFoundException.class, () -> service.addLike(999L, u.getId()));
    }

    @Test
    void addLikeShouldThrowForUnknownUser() {
        Film film = service.create(film("A"));

        assertThrows(NotFoundException.class, () -> service.addLike(film.getId(), 999L));
    }

    @Test
    void removeLikeShouldRemoveLike() {
        Film film = service.create(film("A"));
        User u = user("user1");
        service.addLike(film.getId(), u.getId());
        service.removeLike(film.getId(), u.getId());

        assertTrue(service.getById(film.getId()).getLikes().isEmpty());
    }

    @Test
    void removeLikeShouldNotThrowIfAbsent() {
        Film film = service.create(film("A"));
        User u = user("user1");

        assertDoesNotThrow(() -> service.removeLike(film.getId(), u.getId()));
    }

    @Test
    void getPopularShouldSortByLikes() {
        User u1 = user("user1");
        User u2 = user("user2");
        Film a = service.create(film("A"));
        Film b = service.create(film("B"));
        Film c = service.create(film("C"));
        service.addLike(a.getId(), u1.getId());
        service.addLike(a.getId(), u2.getId());
        service.addLike(b.getId(), u1.getId());
        List<Film> popular = service.getPopular(2);

        assertEquals(2, popular.size());
        assertEquals(a.getId(), popular.get(0).getId());
        assertEquals(b.getId(), popular.get(1).getId());
    }

    @Test
    void getPopularShouldRejectNonPositiveCount() {
        assertThrows(ValidationException.class, () -> service.getPopular(0));
        assertThrows(ValidationException.class, () -> service.getPopular(-1));
    }
}