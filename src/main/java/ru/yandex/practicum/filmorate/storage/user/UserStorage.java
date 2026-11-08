package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import java.util.List;
import java.util.Optional;

public interface UserStorage {
    User getById(Long id);

    List<User> findAll();

    User add(User user);

    User update(User user);

    void delete(Long id);

    void addFriend(Long userId, Long friendId, FriendshipStatus status);

    void removeFriend(Long userId, Long friendId);

    Optional<FriendshipStatus> findFriendshipStatus(Long userId, Long friendId);

    List<User> findFriends(Long userId);

    List<User> findCommonFriends(Long userId, Long otherId);
}