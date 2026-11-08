package ru.yandex.practicum.filmorate;

import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

class FilmControllerTest {
    private FilmController controller;
    private InMemoryUserStorage userStorage;
    private Film validFilm;

    @BeforeEach
    void setUp() {
        userStorage = new InMemoryUserStorage();
        controller = new FilmController(new FilmService(new InMemoryFilmStorage(), userStorage));
        validFilm = new Film();
        validFilm.setName("Шрек");
        validFilm.setDescription("Мультфильм про великана");
        validFilm.setReleaseDate(LocalDate.of(2001, 4, 22));
        validFilm.setDuration(90);
    }

    @Test
    void shouldAddValidFilm() {
        Film saved = controller.create(validFilm);
        assertNotNull(saved.getId());

        assertEquals(1L, saved.getId());
        assertEquals("Шрек", saved.getName());
    }

    @Test
    void shouldAssignIncrementingIds() {
        Film firstFilm = new Film();
        firstFilm.setName("Интерстеллар");
        firstFilm.setDescription("Фантастика о космосе");
        firstFilm.setReleaseDate(LocalDate.of(2014, 11, 7));
        firstFilm.setDuration(169);
        Film secondFilm = new Film();
        secondFilm.setName("Начало");
        secondFilm.setDescription("Про сны");
        secondFilm.setReleaseDate(LocalDate.of(2010, 7, 16));
        secondFilm.setDuration(148);
        Film first = controller.create(firstFilm);
        Film second = controller.create(secondFilm);

        assertEquals(1L, first.getId());
        assertEquals(2L, second.getId());
    }

    @Test
    void shouldReturnEmptyListBeforeAnyFilmAdded() {
        assertTrue(controller.findAll().isEmpty());
    }

    @Test
    void shouldReturnAllFilms() {
        controller.create(validFilm);
        controller.create(validFilm);
        List<Film> films = controller.findAll();

        assertEquals(2, films.size());
    }

    @Test
    void shouldRejectInvalidName() {
        validFilm.setName("");

        assertThrows(ValidationException.class, () -> controller.create(validFilm));
    }

    @Test
    void shouldRejectMoreThan200CharsDescription() {
        validFilm.setDescription("a".repeat(201));

        assertThrows(ValidationException.class, () -> controller.create(validFilm));
    }

    @Test
    void shouldRejectReleaseDateBefore1895() {
        validFilm.setReleaseDate(LocalDate.of(1895, 12, 27));

        assertThrows(ValidationException.class, () -> controller.create(validFilm));
    }

    @Test
    void shouldRejectNonPositiveDuration() {
        validFilm.setDuration(0);

        assertThrows(ValidationException.class, () -> controller.create(validFilm));
    }

    @Test
    void shouldNotSaveInvalidFilm() {
        validFilm.setName("");

        assertThrows(ValidationException.class, () -> controller.create(validFilm));
        assertTrue(controller.findAll().isEmpty());
    }

    @Test
    void shouldUpdateExistingFilm() {
        Film saved = controller.create(validFilm);
        saved.setName("Обновлённое имя");
        Film updated = controller.update(saved);

        assertEquals("Обновлённое имя", updated.getName());
        assertEquals(1, controller.findAll().size());
    }

    @Test
    void shouldRejectUpdateForUnknownFilm() {
        validFilm.setId(10L);

        assertThrows(NotFoundException.class, () -> controller.update(validFilm));
    }

    @Test
    void shouldRejectUpdateWithoutId() {
        validFilm.setId(null);

        assertThrows(NotFoundException.class, () -> controller.update(validFilm));
    }

    @Test
    void shouldRejectUpdateWithInvalidData() {
        Film saved = controller.create(validFilm);
        saved.setName("");

        assertThrows(ValidationException.class, () -> controller.update(saved));
    }

    @Test
    void shouldReturnFilmById() {
        Film saved = controller.create(validFilm);

        assertEquals(saved, controller.getById(saved.getId()));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenGetByIdNotFound() {
        assertThrows(NotFoundException.class, () -> controller.getById(999L));
    }

    private User makeUser(String login) {
        User u = new User();
        u.setEmail(login + "@example.com");
        u.setLogin(login);
        u.setName(login);
        u.setBirthday(LocalDate.of(1990, 1, 1));

        return userStorage.add(u);
    }

    @Test
    void shouldAddLike() {
        Film film = controller.create(validFilm);
        User user = makeUser("u1");
        controller.addLike(film.getId(), user.getId());

        assertEquals(1, controller.getById(film.getId()).getLikes().size());
        assertTrue(controller.getById(film.getId()).getLikes().contains(user.getId()));
    }

    @Test
    void shouldNotDuplicateLike() {
        Film film = controller.create(validFilm);
        User user = makeUser("u1");
        controller.addLike(film.getId(), user.getId());
        controller.addLike(film.getId(), user.getId());

        assertEquals(1, controller.getById(film.getId()).getLikes().size());
    }

    @Test
    void shouldThrowAddLikeWhenUnknownFilm() {
        User user = makeUser("u1");

        assertThrows(NotFoundException.class, () -> controller.addLike(999L, user.getId()));
    }

    @Test
    void shouldThrowAddLikeWhenUnknownUser() {
        Film film = controller.create(validFilm);

        assertThrows(NotFoundException.class, () -> controller.addLike(film.getId(), 999L));
    }

    @Test
    void shouldRemoveLike() {
        Film film = controller.create(validFilm);
        User user = makeUser("u1");
        controller.addLike(film.getId(), user.getId());
        controller.removeLike(film.getId(), user.getId());
        assertTrue(controller.getById(film.getId()).getLikes().isEmpty());
    }

    @Test
    void shouldNotThrowWhenRemoveAbsentLike() {
        Film film = controller.create(validFilm);
        User user = makeUser("u1");

        assertDoesNotThrow(() -> controller.removeLike(film.getId(), user.getId()));
    }

    @Test
    void shouldReturnPopularSortedByLikes() {
        User u1 = makeUser("u1");
        User u2 = makeUser("u2");

        Film a = controller.create(validFilm);
        Film b = new Film();

        b.setName("Начало");
        b.setDescription("Про сны");
        b.setReleaseDate(LocalDate.of(2010, 7, 16));
        b.setDuration(148);
        b = controller.create(b);

        Film c = new Film();
        c.setName("Интерстеллар");
        c.setDescription("Космос");
        c.setReleaseDate(LocalDate.of(2014, 11, 7));
        c.setDuration(169);
        c = controller.create(c);

        controller.addLike(a.getId(), u1.getId());
        controller.addLike(a.getId(), u2.getId());
        controller.addLike(b.getId(), u1.getId());

        List<Film> popular = controller.getPopular(2);

        assertEquals(2, popular.size());
        assertEquals(a.getId(), popular.get(0).getId());
        assertEquals(b.getId(), popular.get(1).getId());
    }
}