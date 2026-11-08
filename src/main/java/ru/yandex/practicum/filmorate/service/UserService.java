package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.validator.ValidationResult;
import ru.yandex.practicum.filmorate.validator.Validator;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User getById(Long id) {
        return userStorage.getById(id);
    }

    public List<User> findAll() {
        return userStorage.findAll();
    }

    public User create(User user) {
        validate(user);
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        User saved = userStorage.add(user);
        log.info("Создан User: id={}, login=\"{}\"", saved.getId(), saved.getLogin());
        return saved;
    }

    public User update(User user) {
        validate(user);
        User existing = userStorage.getById(user.getId());
        user.setFriends(new HashSet<>(existing.getFriends()));
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        User updated = userStorage.update(user);
        log.info("Обновлён User: id={}, login=\"{}\"", updated.getId(), updated.getLogin());
        return updated;
    }

    public Friendship addFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            log.warn("Попытка добавить самого себя в друзья: userId={}", userId);
            throw new ValidationException("Запрещено добавлять самого себя в друзья");
        }
        userStorage.getById(userId);
        userStorage.getById(friendId);

        Optional<FriendshipStatus> existing = userStorage.findFriendshipStatus(userId, friendId);
        if (existing.isPresent() && existing.get() == FriendshipStatus.CONFIRMED) {
            log.info("Дружба уже CONFIRMED: userId={}, friendId={}", userId, friendId);
            return new Friendship(userId, friendId, FriendshipStatus.CONFIRMED);
        }

        boolean incomingFriendRequest = userStorage.findFriendshipStatus(friendId, userId).isPresent();
        FriendshipStatus status = incomingFriendRequest ? FriendshipStatus.CONFIRMED : FriendshipStatus.PENDING;
        userStorage.addFriend(userId, friendId, status);

        if (incomingFriendRequest) {
            userStorage.addFriend(friendId, userId, FriendshipStatus.CONFIRMED);
            log.info("Встречная заявка — дружба CONFIRMED: userId={}, friendId={}", userId, friendId);
        } else {
            log.info("Отправлена заявка в друзья: userId={}, friendId={}, status=PENDING", userId, friendId);
        }
        return new Friendship(userId, friendId, status);
    }

    public void removeFriend(Long userId, Long friendId) {
        userStorage.getById(userId);
        userStorage.getById(friendId);
        userStorage.removeFriend(userId, friendId);
        log.info("User userId={} удалил User friendId={} из друзей", userId, friendId);
    }

    public List<User> getFriends(Long userId) {
        userStorage.getById(userId);
        return userStorage.findFriends(userId);              // 1 SQL, не N+1
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
        userStorage.getById(userId);
        userStorage.getById(otherId);
        List<User> common = userStorage.findCommonFriends(userId, otherId);
        log.info("Общие друзья User userId={} и User otherId={}: их ids={}",
                userId, otherId, common.stream().map(User::getId).toList());
        return common;
    }

    public void delete(Long id) {
        userStorage.getById(id);
        userStorage.delete(id);
        log.info("Удалён User: id={}", id);
    }

    private void validate(User user) {
        ValidationResult result = Validator.validate(user);
        if (!result.isValid()) {
            log.warn("User не валиден: login='{}', reason: {}", user.getLogin(), result.getMessage());
            throw new ValidationException(result.getMessage());
        }
    }
}