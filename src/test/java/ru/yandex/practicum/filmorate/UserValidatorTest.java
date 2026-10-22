package ru.yandex.practicum.filmorate;

import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validator.UserValidator;
import ru.yandex.practicum.filmorate.validator.ValidationResult;

class UserValidatorTest {

    private UserValidator validator;
    private User user;

    @BeforeEach
    void setUp() {
        validator = new UserValidator();
        user = new User();
        user.setEmail("user@example.com");
        user.setLogin("userlogin");
        user.setName("Иван");
        user.setBirthday(LocalDate.of(1990, 5, 15));
    }

    @Test
    void shouldPassValidUser() {
        ValidationResult result = validator.validate(user);
        assertTrue(result.isValid());
        assertNull(result.getMessage());
    }

    @Test
    void shouldRejectNullEmail() {
        user.setEmail(null);
        ValidationResult result = validator.validate(user);
        assertFalse(result.isValid());
    }

    @Test
    void shouldRejectEmptyEmail() {
        user.setEmail("");
        assertFalse(validator.validate(user).isValid());
    }

    @Test
    void shouldRejectBlankEmail() {
        user.setEmail("   ");
        assertFalse(validator.validate(user).isValid());
    }

    @Test
    void shouldRejectEmailWithoutAt() {
        user.setEmail("invalid-email");
        assertFalse(validator.validate(user).isValid());
    }

    @Test
    void shouldAcceptEmailWithAt() {
        user.setEmail("a@b");
        assertTrue(validator.validate(user).isValid());
    }

    @Test
    void shouldRejectNullLogin() {
        user.setLogin(null);
        assertFalse(validator.validate(user).isValid());
    }

    @Test
    void shouldRejectEmptyLogin() {
        user.setLogin("");
        assertFalse(validator.validate(user).isValid());
    }

    @Test
    void shouldRejectBlankLogin() {
        user.setLogin("   ");
        assertFalse(validator.validate(user).isValid());
    }

    @Test
    void shouldRejectLoginWithSpaces() {
        user.setLogin("user login");
        ValidationResult result = validator.validate(user);
        assertFalse(result.isValid());
        assertEquals("Логин не может быть пустым и содержать пробелы", result.getMessage());
    }

    @Test
    void shouldAcceptLoginWithoutSpaces() {
        user.setLogin("userlogin");
        assertTrue(validator.validate(user).isValid());
    }

    @Test
    void shouldRejectBirthdayInFuture() {
        user.setBirthday(LocalDate.now().plusDays(1));
        ValidationResult result = validator.validate(user);
        assertFalse(result.isValid());
    }

    @Test
    void shouldAcceptNullBirthday() {
        user.setBirthday(null);
        assertTrue(validator.validate(user).isValid());
    }

    @Test
    void shouldAcceptBirthdayInPast() {
        user.setBirthday(LocalDate.of(1900, 1, 1));
        assertTrue(validator.validate(user).isValid());
    }
}