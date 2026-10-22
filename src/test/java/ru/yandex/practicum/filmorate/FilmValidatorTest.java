package ru.yandex.practicum.filmorate;

import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.validator.FilmValidator;
import ru.yandex.practicum.filmorate.validator.ValidationResult;

class FilmValidatorTest {
    private FilmValidator validator;
    private Film film;

    @BeforeEach
    void setUp() {
        validator = new FilmValidator();
        film = new Film();
        film.setName("Шрек");
        film.setDescription("Мультфильм про великана");
        film.setReleaseDate(LocalDate.of(2001, 4, 22));
        film.setDuration(90);
    }

    @Test
    void shouldPassValidFilm() {
        ValidationResult result = validator.validate(film);
        assertTrue(result.isValid());
        assertNull(result.getMessage());
    }

    @Test
    void shouldRejectNullName() {
        film.setName(null);
        ValidationResult result = validator.validate(film);
        assertFalse(result.isValid());
        assertEquals("Название фильма не может быть пустым", result.getMessage());
    }

    @Test
    void shouldRejectEmptyName() {
        film.setName("");
        ValidationResult result = validator.validate(film);
        assertFalse(result.isValid());
    }

    @Test
    void shouldRejectBlankName() {
        film.setName("   ");
        ValidationResult result = validator.validate(film);
        assertFalse(result.isValid());
    }

    @Test
    void shouldAcceptDescription200Chars() {
        film.setDescription("a".repeat(200));
        assertTrue(validator.validate(film).isValid());
    }

    @Test
    void shouldRejectDescription201Chars() {
        film.setDescription("a".repeat(201));
        ValidationResult result = validator.validate(film);
        assertFalse(result.isValid());
    }

    @Test
    void shouldAcceptNullDescription() {
        film.setDescription(null);
        assertTrue(validator.validate(film).isValid());
    }

    @Test
    void shouldRejectNullReleaseDate() {
        film.setReleaseDate(null);
        ValidationResult result = validator.validate(film);
        assertFalse(result.isValid());
    }

    @Test
    void shouldRejectReleaseDate1895_12_27() {
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        ValidationResult result = validator.validate(film);
        assertFalse(result.isValid());
        assertEquals("Дата релиза фильма не может быть раньше 28 декабря 1895 года", result.getMessage());
    }

    @Test
    void shouldAcceptReleaseDate1895_12_28() {
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        assertTrue(validator.validate(film).isValid());
    }

    @Test
    void shouldRejectZeroDuration() {
        film.setDuration(0);
        ValidationResult result = validator.validate(film);
        assertFalse(result.isValid());
    }

    @Test
    void shouldRejectNegativeDuration() {
        film.setDuration(-1);
        ValidationResult result = validator.validate(film);
        assertFalse(result.isValid());
    }

    @Test
    void shouldAcceptPositiveDuration() {
        film.setDuration(1);
        assertTrue(validator.validate(film).isValid());
    }
}