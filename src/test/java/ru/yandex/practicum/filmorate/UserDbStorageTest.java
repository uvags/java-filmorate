package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import java.time.LocalDate;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@Import(UserDbStorage.class)
class UserDbStorageTest {
    @Autowired private UserDbStorage userStorage;
    @Autowired private JdbcTemplate jdbc;

    private User newUser(String login) {
        User u = new User();
        u.setEmail(login + "@test.ru");
        u.setLogin(login);
        u.setName(login);
        u.setBirthday(LocalDate.of(1990, 1, 1));
        return u;
    }

    @Test
    void shouldAddAndFindUser() {
        User saved = userStorage.add(newUser("a"));
        assertThat(saved.getId()).isNotNull();
        assertThat(userStorage.getById(saved.getId()).getLogin()).isEqualTo("a");
    }

    @Test
    void shouldAddFriendPending() {
        User a = userStorage.add(newUser("a"));
        User b = userStorage.add(newUser("b"));

        userStorage.addFriend(a.getId(), b.getId(), FriendshipStatus.PENDING);

        assertThat(userStorage.findFriendshipStatus(a.getId(), b.getId()))
                .hasValue(FriendshipStatus.PENDING);
        assertThat(userStorage.findFriends(a.getId()))
                .extracting(User::getId).containsExactly(b.getId());
        assertThat(userStorage.findFriends(b.getId())).isEmpty();
    }

    @Test
    void shouldUpdateFriendshipStatusToConfirmed() {
        User a = userStorage.add(newUser("a"));
        User b = userStorage.add(newUser("b"));

        userStorage.addFriend(a.getId(), b.getId(), FriendshipStatus.PENDING);
        userStorage.addFriend(a.getId(), b.getId(), FriendshipStatus.CONFIRMED);

        assertThat(userStorage.findFriendshipStatus(a.getId(), b.getId()))
                .hasValue(FriendshipStatus.CONFIRMED);
    }

    @Test
    void shouldReturnCommonFriends() {
        User a = userStorage.add(newUser("a"));
        User b = userStorage.add(newUser("b"));
        User c = userStorage.add(newUser("c"));

        userStorage.addFriend(a.getId(), c.getId(), FriendshipStatus.CONFIRMED);
        userStorage.addFriend(b.getId(), c.getId(), FriendshipStatus.CONFIRMED);

        List<User> common = userStorage.findCommonFriends(a.getId(), b.getId());
        assertThat(common).extracting(User::getId).containsExactly(c.getId());
    }

    @Test
    void shouldThrowWhenNotFound() {
        assertThatThrownBy(() -> userStorage.getById(999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldDeleteUser() {
        User saved = userStorage.add(newUser("a"));

        userStorage.delete(saved.getId());

        assertThatThrownBy(() -> userStorage.getById(saved.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldDeleteUserCascadeOwnFriendships() {
        User a = userStorage.add(newUser("a"));
        User b = userStorage.add(newUser("b"));
        userStorage.addFriend(a.getId(), b.getId(), FriendshipStatus.PENDING);

        userStorage.delete(a.getId());

        Integer remaining = jdbc.queryForObject(
                "SELECT COUNT(*) FROM friendships WHERE user_id = ?",
                Integer.class, a.getId());
        assertThat(remaining).isZero();
    }

    @Test
    void shouldDeleteUserCascadeIncomingFriendships() {
        User a = userStorage.add(newUser("a"));
        User b = userStorage.add(newUser("b"));
        userStorage.addFriend(b.getId(), a.getId(), FriendshipStatus.PENDING);

        userStorage.delete(a.getId());

        Integer remaining = jdbc.queryForObject(
                "SELECT COUNT(*) FROM friendships WHERE friend_id = ?",
                Integer.class, a.getId());

        assertThat(remaining).isZero();
        assertThat(userStorage.findFriends(b.getId())).isEmpty();
    }

    @Test
    void shouldDeleteUserCascadeLikes() {
        User user = userStorage.add(newUser("a"));

        jdbc.update("INSERT INTO films (name, description, release_date, duration) " +
                "VALUES ('X', 'd', '2000-01-01', 90)");
        long filmId = jdbc.queryForObject("SELECT id FROM films WHERE name='X'", Long.class);
        jdbc.update("INSERT INTO likes (film_id, user_id) VALUES (?, ?)", filmId, user.getId());

        userStorage.delete(user.getId());

        Integer remaining = jdbc.queryForObject(
                "SELECT COUNT(*) FROM likes WHERE user_id = ?",
                Integer.class, user.getId());

        assertThat(remaining).isZero();
    }

    @Test
    void shouldNotAffectOtherUsersOnDelete() {
        User toDelete = userStorage.add(newUser("a"));
        User keepUser = userStorage.add(newUser("b"));

        userStorage.delete(toDelete.getId());

        assertThat(userStorage
                .findAll())
                .extracting(User::getId)
                .containsExactly(keepUser.getId());
    }

    @Test
    void shouldThrowWhenDeleteNotFound() {
        assertThatThrownBy(() -> userStorage.delete(999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldUpdateUser() {
        User saved = userStorage.add(newUser("a"));
        saved.setEmail("new@test.ru");
        saved.setLogin("newlogin");
        saved.setName("Новое имя");
        saved.setBirthday(LocalDate.of(1995, 5, 5));
        userStorage.update(saved);
        User found = userStorage.getById(saved.getId());

        assertThat(found.getEmail()).isEqualTo("new@test.ru");
        assertThat(found.getLogin()).isEqualTo("newlogin");
        assertThat(found.getName()).isEqualTo("Новое имя");
        assertThat(found.getBirthday()).isEqualTo(LocalDate.of(1995, 5, 5));
    }

    @Test
    void shouldUpdateUserPreserveFriendships() {
        User a = userStorage.add(newUser("a"));
        User b = userStorage.add(newUser("b"));
        userStorage.addFriend(a.getId(), b.getId(), FriendshipStatus.PENDING);
        a.setName("Обновлён");
        userStorage.update(a);

        assertThat(userStorage.findFriendshipStatus(a.getId(), b.getId()))
                .hasValue(FriendshipStatus.PENDING);
        assertThat(userStorage.findFriends(a.getId()))
                .extracting(User::getId).containsExactly(b.getId());
    }

    @Test
    void shouldThrowWhenUpdateNotFound() {
        User ghost = newUser("ghost");
        ghost.setId(999L);

        assertThatThrownBy(() -> userStorage.update(ghost))
                .isInstanceOf(NotFoundException.class);
    }
}