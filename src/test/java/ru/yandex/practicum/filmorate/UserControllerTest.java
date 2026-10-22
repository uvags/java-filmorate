package ru.yandex.practicum.filmorate;

import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

class UserControllerTest {

    private UserController controller;
    private User validUser;

    @BeforeEach
    void setUp() {
        controller = new UserController();
        validUser = new User();
        validUser.setEmail("user@example.com");
        validUser.setLogin("userlogin");
        validUser.setName("Иван");
        validUser.setBirthday(LocalDate.of(1990, 5, 15));
    }

    @Test
    void shouldCreateValidUser() {
        User saved = controller.createUser(validUser);
        assertNotNull(saved.getId());
        assertEquals(1L, saved.getId());
        assertEquals("Иван", saved.getName());
    }

    @Test
    void shouldAssignIncrementingIds() {
        User firstUser = new User();
        firstUser.setEmail("user1@example.com");
        firstUser.setLogin("login1");
        firstUser.setName("Иван");
        firstUser.setBirthday(LocalDate.of(1990, 5, 15));

        User secondUser = new User();
        secondUser.setEmail("user2@example.com");
        secondUser.setLogin("login2");
        secondUser.setName("Пётр");
        secondUser.setBirthday(LocalDate.of(1991, 6, 20));

        User first = controller.createUser(firstUser);
        User second = controller.createUser(secondUser);

        assertEquals(1L, first.getId());
        assertEquals(2L, second.getId());
    }

    @Test
    void shouldUseLoginWhenNameIsBlank() {
        validUser.setName(" ");
        User saved = controller.createUser(validUser);
        assertEquals("userlogin", saved.getName());
    }

    @Test
    void shouldUseLoginWhenNameIsNull() {
        validUser.setName(null);
        User saved = controller.createUser(validUser);
        assertEquals("userlogin", saved.getName());
    }

    @Test
    void shouldKeepNameWhenProvided() {
        validUser.setName("Пётр");
        User saved = controller.createUser(validUser);
        assertEquals("Пётр", saved.getName());
    }

    @Test
    void shouldReturnEmptyListInitially() {
        assertTrue(controller.findAll().isEmpty());
    }

    @Test
    void shouldReturnAllUsers() {
        controller.createUser(validUser);
        controller.createUser(validUser);
        List<User> users = controller.findAll();
        assertEquals(2, users.size());
    }

    @Test
    void shouldRejectEmailWithoutAt() {
        validUser.setEmail("invalid-email");
        assertThrows(ValidationException.class, () -> controller.createUser(validUser));
    }

    @Test
    void shouldRejectEmptyEmail() {
        validUser.setEmail("");
        assertThrows(ValidationException.class, () -> controller.createUser(validUser));
    }

    @Test
    void shouldRejectLoginWithSpaces() {
        validUser.setLogin("user login");
        assertThrows(ValidationException.class, () -> controller.createUser(validUser));
    }

    @Test
    void shouldRejectEmptyLogin() {
        validUser.setLogin("");
        assertThrows(ValidationException.class, () -> controller.createUser(validUser));
    }

    @Test
    void shouldRejectBirthdayInFuture() {
        validUser.setBirthday(LocalDate.now().plusDays(1));
        assertThrows(ValidationException.class, () -> controller.createUser(validUser));
    }

    @Test
    void shouldAcceptBirthdayToday() {
        validUser.setBirthday(LocalDate.now());
        assertDoesNotThrow(() -> controller.createUser(validUser));
    }

    @Test
    void shouldAcceptNullBirthday() {
        validUser.setBirthday(null);
        assertDoesNotThrow(() -> controller.createUser(validUser));
    }

    @Test
    void shouldNotSaveInvalidUser() {
        validUser.setEmail("bad");
        assertThrows(ValidationException.class, () -> controller.createUser(validUser));
        assertTrue(controller.findAll().isEmpty());
    }

    @Test
    void shouldUpdateExistingUser() {
        User saved = controller.createUser(validUser);
        saved.setName("Пётр");
        User updated = controller.updateUser(saved);
        assertEquals("Пётр", updated.getName());
        assertEquals(1, controller.findAll().size());
    }

    @Test
    void shouldRejectUpdateForUnknownUser() {
        validUser.setId(999L);
        assertThrows(NotFoundException.class, () -> controller.updateUser(validUser));
    }

    @Test
    void shouldRejectUpdateWithoutId() {
        validUser.setId(null);
        assertThrows(NotFoundException.class, () -> controller.updateUser(validUser));
    }

    @Test
    void shouldRejectUpdateWithInvalidData() {
        User saved = controller.createUser(validUser);
        saved.setEmail("bad");
        assertThrows(ValidationException.class, () -> controller.updateUser(saved));
    }
}