package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import java.util.*;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private final Map<Long, Map<Long, FriendshipStatus>> friendships = new HashMap<>();
    private Long nextId = 0L;

    @Override
    public User getById(Long id) {
        User user = users.get(id);
        if (user == null) {
            log.warn("User не найден: id={}", id);
            throw new NotFoundException("User с id=" + id + " не найден");
        }
        return user;
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User add(User user) {
        user.setId(++nextId);
        users.put(user.getId(), user);
        log.info("Добавлен User: id={}, login=\"{}\"", user.getId(), user.getLogin());
        return user;
    }

    @Override
    public User update(User user) {
        if (user.getId() == null || !users.containsKey(user.getId())) {
            log.warn("Попытка обновить данные несуществующего User: id={}", user.getId());
            throw new NotFoundException("Пользователь с id=" + user.getId() + " не найден");
        }
        users.put(user.getId(), user);
        log.info("Обновлены данные User: id={}, login=\"{}\"", user.getId(), user.getLogin());
        return user;
    }

    @Override
    public void delete(Long id) {
        User removed = users.remove(id);
        friendships.remove(id);
        friendships.values().forEach(m -> m.remove(id));
        users.values().forEach(u -> u.getFriends().remove(id));
        if (removed != null) {
            log.info("Удалены данные User: id={}, login=\"{}\"", id, removed.getLogin());
        }
    }

    @Override
    public void addFriend(Long userId, Long friendId, FriendshipStatus status) {
        friendships.computeIfAbsent(userId, k -> new HashMap<>()).put(friendId, status);
        User user = users.get(userId);
        if (user != null) {
            user.getFriends().add(friendId);
        }
        log.info("Сохранена дружба: userId={}, friendId={}, status={}", userId, friendId, status);
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        Map<Long, FriendshipStatus> m = friendships.get(userId);
        if (m != null) m.remove(friendId);
        User user = users.get(userId);
        if (user != null) user.getFriends().remove(friendId);
        log.info("Удалена дружба: userId={}, friendId={}", userId, friendId);
    }

    @Override
    public Optional<FriendshipStatus> findFriendshipStatus(Long userId, Long friendId) {
        return Optional.ofNullable(friendships.getOrDefault(userId, Map.of()).get(friendId));
    }

    @Override
    public List<User> findFriends(Long userId) {
        return users.get(userId).getFriends()
                .stream()
                .map(users::get)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<User> findCommonFriends(Long userId, Long otherId) {
        Set<Long> otherFriends = users.get(otherId).getFriends();
        return users.get(userId).getFriends().stream()
                .filter(otherFriends::contains)
                .map(users::get)
                .filter(Objects::nonNull)
                .toList();
    }
}