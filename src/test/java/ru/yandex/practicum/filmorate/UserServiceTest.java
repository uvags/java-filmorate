package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {
    private UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService(new InMemoryUserStorage());
    }

    @Test
    void createShouldAssignId() {
        User saved = service.create(user("u"));
        assertNotNull(saved.getId());
        assertEquals(saved, service.getById(saved.getId()));
    }

    @Test
    void createShouldUseLoginWhenNameBlank() {
        User u = user("u");
        u.setName(" ");
        assertEquals("u", service.create(u).getName());
    }

    @Test
    void createShouldUseLoginWhenNameNull() {
        User u = user("uselogin");
        u.setName(null);
        assertEquals("uselogin", service.create(u).getName());
    }

    @Test
    void createShouldRejectInvalidUser() {
        User invalid = user("u");
        invalid.setEmail("bad");
        assertThrows(ValidationException.class, () -> service.create(invalid));
        assertTrue(service.findAll().isEmpty());
    }

    @Test
    void getByIdShouldThrowWhenNotFound() {
        assertThrows(NotFoundException.class, () -> service.getById(999L));
    }

    @Test
    void findAllShouldReturnAllUsers() {
        service.create(user("a"));
        service.create(user("b"));
        assertEquals(2, service.findAll().size());
    }

    @Test
    void updateShouldUpdateUser() {
        User saved = service.create(user("a"));
        saved.setName("B");
        assertEquals("B", service.update(saved).getName());
    }

    @Test
    void updateShouldThrowForUnknownUser() {
        User u = user("a");
        u.setId(999L);
        assertThrows(NotFoundException.class, () -> service.update(u));
    }

    @Test
    void addFriendShouldBeOneWay() {
        User a = service.create(user("a"));
        User b = service.create(user("b"));
        service.addFriend(a.getId(), b.getId());

        // у a есть b
        assertTrue(service.getById(a.getId()).getFriends().contains(b.getId()));
        // у b НЕТ a (односторонняя дружба)
        assertFalse(service.getById(b.getId()).getFriends().contains(a.getId()));
    }

    @Test
    void shouldConfirmFriendshipOnMutualRequest() {
        User a = service.create(user("a"));
        User b = service.create(user("b"));

        Friendship first = service.addFriend(a.getId(), b.getId());
        assertEquals(FriendshipStatus.PENDING, first.getStatus());

        Friendship second = service.addFriend(b.getId(), a.getId());
        assertEquals(FriendshipStatus.CONFIRMED, second.getStatus());

        // теперь у обоих есть друг
        assertEquals(1, service.getFriends(a.getId()).size());
        assertEquals(1, service.getFriends(b.getId()).size());
    }

    @Test
    void addFriendShouldRejectSelf() {
        User a = service.create(user("a"));
        assertThrows(ValidationException.class, () -> service.addFriend(a.getId(), a.getId()));
    }

    @Test
    void addFriendShouldThrowForUnknownUser() {
        User a = service.create(user("a"));
        assertThrows(NotFoundException.class, () -> service.addFriend(a.getId(), 999L));
    }

    @Test
    void shouldRemoveFriendFromSender() {
        User a = service.create(user("a"));
        User b = service.create(user("b"));
        service.addFriend(a.getId(), b.getId());
        service.removeFriend(a.getId(), b.getId());

        assertFalse(service.getById(a.getId()).getFriends().contains(b.getId()));
    }

    @Test
    void getFriendsShouldReturnFriends() {
        User a = service.create(user("a"));
        User b = service.create(user("b"));
        service.addFriend(a.getId(), b.getId());
        List<User> friends = service.getFriends(a.getId());
        assertEquals(1, friends.size());
        assertEquals(b.getId(), friends.getFirst().getId());
    }

    @Test
    void getFriendsShouldThrowForUnknownUser() {
        assertThrows(NotFoundException.class, () -> service.getFriends(999L));
    }

    @Test
    void getCommonFriendsShouldReturnCommon() {
        User a = service.create(user("a"));
        User b = service.create(user("b"));
        User c = service.create(user("c"));
        service.addFriend(a.getId(), c.getId());
        service.addFriend(b.getId(), c.getId());
        List<User> common = service.getCommonFriends(a.getId(), b.getId());
        assertEquals(1, common.size());
        assertEquals(c.getId(), common.getFirst().getId());
    }

    @Test
    void getCommonFriendsShouldReturnEmptyWhenNone() {
        User a = service.create(user("a"));
        User b = service.create(user("b"));
        assertTrue(service.getCommonFriends(a.getId(), b.getId()).isEmpty());
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