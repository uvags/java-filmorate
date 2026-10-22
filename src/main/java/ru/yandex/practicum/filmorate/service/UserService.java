package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.validator.ValidationResult;
import ru.yandex.practicum.filmorate.validator.Validator;
import java.util.List;
import java.util.Set;

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
        return userStorage.add(user);
    }

    public User update(User user) {
        validate(user);
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return userStorage.update(user);
    }

    // добавление друга без подтверждений
    public void addFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            log.warn("Попытка добавить самого себя в друзья: userId={}", userId);
            throw new ValidationException("Запрещено добавлять самого себя в друзья");
        }
        User user = userStorage.getById(userId);
        User friend = userStorage.getById(friendId);

        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
        log.info("User userId={} добавил User friendId={} в друзья", userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        User user = userStorage.getById(userId);
        User friend = userStorage.getById(friendId);

        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
        log.info("User userId={} удалил User friendId={} из друзей", userId, friendId);
    }

    public List<User> getFriends(Long userId) {
        User user = userStorage.getById(userId);
        return user.getFriends().stream()
                .map(userStorage::getById)
                .toList();
    }

    // получение списка общих друзей с другим User
    public List<User> getCommonFriends(Long userId, Long otherId) {
        User user = userStorage.getById(userId);
        User other = userStorage.getById(otherId);
        Set<Long> otherFriends = other.getFriends();
        List<User> common = user.getFriends().stream()
                .filter(otherFriends::contains)
                .map(userStorage::getById)
                .toList();
        log.info("Общие друзья User userId={} и User otherId={}: их ids={}", userId, otherId,
                common.stream().map(User::getId).toList());
        return common;
    }

    // используем утилитарный класс Validator, хотя можно рассмотреть вариант с бином
    private void validate(User user) {
        ValidationResult result = Validator.validate(user);
        if (!result.isValid()) {
            log.warn("User не валиден: login='{}', reason: {}", user.getLogin(), result.getMessage());
            throw new ValidationException(result.getMessage());
        }
    }
}