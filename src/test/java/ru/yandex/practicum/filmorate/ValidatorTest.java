package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validator.ValidationResult;
import ru.yandex.practicum.filmorate.validator.Validator;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ValidatorTest {
    private Film film;
    private User user;

    @BeforeEach
    void setUp() {
        film = new Film();
        film.setName("Шрек");
        film.setDescription("Мультфильм про великана");
        film.setReleaseDate(LocalDate.of(2001, 4, 22));
        film.setDuration(90);

        user = new User();
        user.setEmail("user@example.com");
        user.setLogin("userlogin");
        user.setName("Иван");
        user.setBirthday(LocalDate.of(1990, 5, 15));
    }

    @Test
    void shouldPassValidFilm() {
        ValidationResult result = Validator.validate(film);
        assertTrue(result.isValid());
        assertNull(result.getMessage());
    }

    @Test
    void shouldRejectNullFilmName() {
        film.setName(null);
        assertFalse(Validator.validate(film).isValid());
    }

    @Test
    void shouldRejectEmptyFilmName() {
        film.setName("");
        assertFalse(Validator.validate(film).isValid());
    }

    @Test
    void shouldRejectBlankFilmName() {
        film.setName("   ");
        assertFalse(Validator.validate(film).isValid());
    }

    @Test
    void shouldAcceptDescription200Chars() {
        film.setDescription("a".repeat(200));
        assertTrue(Validator.validate(film).isValid());
    }

    @Test
    void shouldRejectDescription201Chars() {
        film.setDescription("a".repeat(201));
        assertFalse(Validator.validate(film).isValid());
    }

    @Test
    void shouldAcceptNullDescription() {
        film.setDescription(null);
        assertTrue(Validator.validate(film).isValid());
    }

    @Test
    void shouldRejectNullReleaseDate() {
        film.setReleaseDate(null);
        assertFalse(Validator.validate(film).isValid());
    }

    @Test
    void shouldRejectReleaseDate1895_12_27() {
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        ValidationResult result = Validator.validate(film);
        assertFalse(result.isValid());
        assertEquals("Дата релиза фильма не может быть раньше 28 декабря 1895 года", result.getMessage());
    }

    @Test
    void shouldAcceptReleaseDate1895_12_28() {
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        assertTrue(Validator.validate(film).isValid());
    }

    @Test
    void shouldRejectZeroDuration() {
        film.setDuration(0);
        assertFalse(Validator.validate(film).isValid());
    }

    @Test
    void shouldRejectNegativeDuration() {
        film.setDuration(-1);
        assertFalse(Validator.validate(film).isValid());
    }

    @Test
    void shouldAcceptPositiveDuration() {
        film.setDuration(1);
        assertTrue(Validator.validate(film).isValid());
    }

    @Test
    void shouldPassValidUser() {
        ValidationResult result = Validator.validate(user);
        assertTrue(result.isValid());
        assertNull(result.getMessage());
    }

    @Test
    void shouldRejectNullEmail() {
        user.setEmail(null);
        assertFalse(Validator.validate(user).isValid());
    }

    @Test
    void shouldRejectEmptyEmail() {
        user.setEmail("");
        assertFalse(Validator.validate(user).isValid());
    }

    @Test
    void shouldRejectBlankEmail() {
        user.setEmail("   ");
        assertFalse(Validator.validate(user).isValid());
    }

    @Test
    void shouldRejectEmailWithoutAt() {
        user.setEmail("invalid-email");
        assertFalse(Validator.validate(user).isValid());
    }

    @Test
    void shouldAcceptEmailWithAt() {
        user.setEmail("a@b");
        assertTrue(Validator.validate(user).isValid());
    }

    @Test
    void shouldRejectNullLogin() {
        user.setLogin(null);
        assertFalse(Validator.validate(user).isValid());
    }

    @Test
    void shouldRejectEmptyLogin() {
        user.setLogin("");
        assertFalse(Validator.validate(user).isValid());
    }

    @Test
    void shouldRejectBlankLogin() {
        user.setLogin("   ");
        assertFalse(Validator.validate(user).isValid());
    }

    @Test
    void shouldRejectLoginWithSpaces() {
        user.setLogin("user login");
        ValidationResult result = Validator.validate(user);
        assertFalse(result.isValid());
        assertEquals("Логин не может быть пустым и содержать пробелы", result.getMessage());
    }

    @Test
    void shouldAcceptLoginWithoutSpaces() {
        user.setLogin("userlogin");
        assertTrue(Validator.validate(user).isValid());
    }

    @Test
    void shouldRejectBirthdayInFuture() {
        user.setBirthday(LocalDate.now().plusDays(1));
        assertFalse(Validator.validate(user).isValid());
    }

    @Test
    void shouldAcceptNullBirthday() {
        user.setBirthday(null);
        assertTrue(Validator.validate(user).isValid());
    }

    @Test
    void shouldAcceptBirthdayInPast() {
        user.setBirthday(LocalDate.of(1900, 1, 1));
        assertTrue(Validator.validate(user).isValid());
    }
}