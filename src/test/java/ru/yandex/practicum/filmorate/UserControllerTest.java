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
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

class UserControllerTest {
    private UserController controller;
    private User validUser;

    @BeforeEach
    void setUp() {
        controller = new UserController(new UserService(new InMemoryUserStorage()));
        validUser = new User();
        validUser.setEmail("user@example.com");
        validUser.setLogin("userlogin");
        validUser.setName("Иван");
        validUser.setBirthday(LocalDate.of(1990, 5, 15));
    }

    @Test
    void shouldCreateValidUser() {
        User saved = controller.create(validUser);
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

        User first = controller.create(firstUser);
        User second = controller.create(secondUser);

        assertEquals(1L, first.getId());
        assertEquals(2L, second.getId());
    }

    @Test
    void shouldUseLoginWhenNameIsBlank() {
        validUser.setName(" ");
        User saved = controller.create(validUser);

        assertEquals("userlogin", saved.getName());
    }

    @Test
    void shouldUseLoginWhenNameIsNull() {
        validUser.setName(null);
        User saved = controller.create(validUser);

        assertEquals("userlogin", saved.getName());
    }

    @Test
    void shouldKeepNameWhenProvided() {
        validUser.setName("Пётр");
        User saved = controller.create(validUser);

        assertEquals("Пётр", saved.getName());
    }

    @Test
    void shouldReturnEmptyListInitially() {
        assertTrue(controller.findAll().isEmpty());
    }

    @Test
    void shouldReturnAllUsers() {
        controller.create(validUser);
        controller.create(validUser);
        List<User> users = controller.findAll();

        assertEquals(2, users.size());
    }

    @Test
    void shouldRejectEmailWithoutAt() {
        validUser.setEmail("invalid-email");

        assertThrows(ValidationException.class, () -> controller.create(validUser));
    }

    @Test
    void shouldRejectEmptyEmail() {
        validUser.setEmail("");

        assertThrows(ValidationException.class, () -> controller.create(validUser));
    }

    @Test
    void shouldRejectLoginWithSpaces() {
        validUser.setLogin("user login");

        assertThrows(ValidationException.class, () -> controller.create(validUser));
    }

    @Test
    void shouldRejectEmptyLogin() {
        validUser.setLogin("");

        assertThrows(ValidationException.class, () -> controller.create(validUser));
    }

    @Test
    void shouldRejectBirthdayInFuture() {
        validUser.setBirthday(LocalDate.now().plusDays(1));

        assertThrows(ValidationException.class, () -> controller.create(validUser));
    }

    @Test
    void shouldAcceptBirthdayToday() {
        validUser.setBirthday(LocalDate.now());

        assertDoesNotThrow(() -> controller.create(validUser));
    }

    @Test
    void shouldAcceptNullBirthday() {
        validUser.setBirthday(null);

        assertDoesNotThrow(() -> controller.create(validUser));
    }

    @Test
    void shouldNotSaveInvalidUser() {
        validUser.setEmail("bad");

        assertThrows(ValidationException.class, () -> controller.create(validUser));
        assertTrue(controller.findAll().isEmpty());
    }

    @Test
    void shouldUpdateExistingUser() {
        User saved = controller.create(validUser);
        saved.setName("Пётр");
        User updated = controller.update(saved);

        assertEquals("Пётр", updated.getName());
        assertEquals(1, controller.findAll().size());
    }

    @Test
    void shouldRejectUpdateForUnknownUser() {
        validUser.setId(999L);

        assertThrows(NotFoundException.class, () -> controller.update(validUser));
    }

    @Test
    void shouldRejectUpdateWithoutId() {
        validUser.setId(null);

        assertThrows(NotFoundException.class, () -> controller.update(validUser));
    }

    @Test
    void shouldRejectUpdateWithInvalidData() {
        User saved = controller.create(validUser);
        saved.setEmail("bad");

        assertThrows(ValidationException.class, () -> controller.update(saved));
    }

    @Test
    void shouldReturnUserById() {
        User saved = controller.create(validUser);

        assertEquals(saved, controller.getById(saved.getId()));
    }

    @Test
    void shouldThrowWhenGetByIdNotFound() {
        assertThrows(NotFoundException.class, () -> controller.getById(999L));
    }

    private User makeUser(String login) {
        User u = new User();
        u.setEmail(login + "@example.com");
        u.setLogin(login);
        u.setName(login);
        u.setBirthday(LocalDate.of(1990, 1, 1));

        return u;
    }

    @Test
    void shouldAddFriend() {
        User a = controller.create(makeUser("a"));
        User b = controller.create(makeUser("b"));

        controller.addFriend(a.getId(), b.getId());

        List<User> friends = controller.getFriends(a.getId());

        assertEquals(1, friends.size());
        assertEquals(b.getId(), friends.getFirst().getId());
    }

    @Test
    void shouldBecomeFriendsSymmetrically() {
        User a = controller.create(makeUser("a"));
        User b = controller.create(makeUser("b"));
        controller.addFriend(a.getId(), b.getId());

        List<User> bFriendList = controller.getFriends(b.getId());

        assertEquals(1, bFriendList.size());
        assertEquals(a.getId(), bFriendList.getFirst().getId());
    }

    @Test
    void shouldRejectMyselfAsFriend() {
        User a = controller.create(makeUser("a"));

        assertThrows(ValidationException.class, () -> controller.addFriend(a.getId(), a.getId()));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenAddFriendUnknownUser() {
        User a = controller.create(makeUser("a"));

        assertThrows(NotFoundException.class, () -> controller.addFriend(a.getId(), 999L));
    }

    @Test
    void shouldRemoveGivenFriend() {
        User a = controller.create(makeUser("a"));
        User b = controller.create(makeUser("b"));
        controller.addFriend(a.getId(), b.getId());

        controller.removeFriend(a.getId(), b.getId());

        assertTrue(controller.getFriends(a.getId()).isEmpty());
        assertTrue(controller.getFriends(b.getId()).isEmpty());
    }

    @Test
    void shouldReturnCommonFriends() {
        User a = controller.create(makeUser("a"));
        User b = controller.create(makeUser("b"));
        User c = controller.create(makeUser("c"));

        controller.addFriend(a.getId(), c.getId());
        controller.addFriend(b.getId(), c.getId());

        List<User> common = controller.getCommonFriends(a.getId(), b.getId());

        assertEquals(1, common.size());
        assertEquals(c.getId(), common.getFirst().getId());
    }

    @Test
    void shouldReturnEmptyCommonFriendsWhenNone() {
        User a = controller.create(makeUser("a"));
        User b = controller.create(makeUser("b"));

        assertTrue(controller.getCommonFriends(a.getId(), b.getId()).isEmpty());
    }
}