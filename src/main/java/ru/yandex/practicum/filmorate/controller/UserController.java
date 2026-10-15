package ru.yandex.practicum.filmorate.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validator.UserValidator;
import ru.yandex.practicum.filmorate.validator.ValidationResult;

@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Long, User> users = new HashMap<>();
    private final UserValidator userValidator = new UserValidator();
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @GetMapping
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        ValidationResult result = userValidator.validate(user);
        if (!result.isValid()) {
            log.warn("User не прошёл валидацию: {}", result.getMessage());
            throw new ValidationException(result.getMessage());
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Создан User: {}", user);
        return user;
    }

    @PutMapping
    public User updateUser(@RequestBody User user) {
        ValidationResult result = userValidator.validate(user);
        if (!result.isValid()) {
            log.warn("User не прошёл валидацию: {}", result.getMessage());
            throw new ValidationException(result.getMessage());
        }
        if (user.getId() == null || !users.containsKey(user.getId())) {
            log.warn("User с id {} не найден", user.getId());
            throw new NotFoundException("User не найден");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        users.put(user.getId(), user);
        log.info("Обновлён User: {}", user);
        return user;
    }

    private Long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}