package ru.yandex.practicum.filmorate;

import java.util.List;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {
    private FilmController controller;
    private Film validFilm;

    @BeforeEach
    void setUp() {
        controller = new FilmController();
        validFilm = new Film();
        validFilm.setName("Шрек");
        validFilm.setDescription("Мультфильм про великана");
        validFilm.setReleaseDate(LocalDate.of(2001, 4, 22));
        validFilm.setDuration(90);
    }

    @Test
    void shouldAddValidFilm() {
        Film saved = controller.addFilm(validFilm);
        assertNotNull(saved.getId());
        assertEquals(1L, saved.getId());
        assertEquals("Шрек", saved.getName());
    }

    @Test
    void shouldAssignIncrementingIds() {
        Film first = controller.addFilm(validFilm);
        Film second = controller.addFilm(validFilm);
        assertEquals(1L, first.getId());
        assertEquals(2L, second.getId());
    }

    @Test
    void shouldReturnEmptyListBeforeAnyFilmAdded() {
        assertTrue(controller.findAll().isEmpty());
    }

    @Test
    void shouldReturnAllFilms() {
        controller.addFilm(validFilm);
        controller.addFilm(validFilm);
        List<Film> films = controller.findAll();
        assertEquals(2, films.size());
    }

    @Test
    void shouldRejectInvalidName() {
        validFilm.setName("");
        assertThrows(ValidationException.class, () -> controller.addFilm(validFilm));
    }

    @Test
    void shouldRejectMoreThan200CharsDescription() {
        validFilm.setDescription("a".repeat(201));
        assertThrows(ValidationException.class, () -> controller.addFilm(validFilm));
    }

    @Test
    void shouldRejectReleaseDateBefore1895() {
        validFilm.setReleaseDate(LocalDate.of(1895, 12, 27));
        assertThrows(ValidationException.class, () -> controller.addFilm(validFilm));
    }

    @Test
    void shouldRejectNonPositiveDuration() {
        validFilm.setDuration(0);
        assertThrows(ValidationException.class, () -> controller.addFilm(validFilm));
    }

    @Test
    void shouldNotSaveInvalidFilm() {
        validFilm.setName("");
        assertThrows(ValidationException.class, () -> controller.addFilm(validFilm));
        assertTrue(controller.findAll().isEmpty());
    }

    @Test
    void shouldUpdateExistingFilm() {
        Film saved = controller.addFilm(validFilm);
        saved.setName("Обновлённое имя");
        Film updated = controller.updateFilm(saved);
        assertEquals("Обновлённое имя", updated.getName());
        assertEquals(1, controller.findAll().size());
    }

    @Test
    void shouldRejectUpdateForUnknownFilm() {
        validFilm.setId(10L);
        assertThrows(NotFoundException.class, () -> controller.updateFilm(validFilm));
    }

    @Test
    void shouldRejectUpdateWithoutId() {
        validFilm.setId(null);
        assertThrows(NotFoundException.class, () -> controller.updateFilm(validFilm));
    }

    @Test
    void shouldRejectUpdateWithInvalidData() {
        Film saved = controller.addFilm(validFilm);
        saved.setName("");
        assertThrows(ValidationException.class, () -> controller.updateFilm(saved));
    }
}