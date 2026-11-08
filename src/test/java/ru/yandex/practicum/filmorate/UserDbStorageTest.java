package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
class UserDbStorageTest {
    @Autowired private UserDbStorage userStorage;

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
}