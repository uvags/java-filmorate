package ru.yandex.practicum.filmorate.validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

public class UserValidator {
    private static final Logger log = LoggerFactory.getLogger(UserValidator.class);

    public ValidationResult validate(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.warn("Ошибка валидации: некорректный email");
            return ValidationResult.error(
                    "email не может быть пустой и должна содержать символ @");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.warn("Ошибка валидации: некорректный логин");
            return ValidationResult.error("Логин не может быть пустым и содержать пробелы");
        }
        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Ошибка валидации: некорректная дата рождения");
            return ValidationResult.error("Дата рождения не может быть позже настоящего дня");
        }
        return ValidationResult.ok();
    }
}