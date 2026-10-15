package ru.yandex.practicum.filmorate.validator;

import ru.yandex.practicum.filmorate.model.User;
import java.time.LocalDate;

public class UserValidator {
    public ValidationResult validate(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            return ValidationResult.error(
                    "email не может быть пустым и должен содержать символ @");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            return ValidationResult.error("Логин не может быть пустым и содержать пробелы");
        }
        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            return ValidationResult.error("Дата рождения не может быть позже настоящего дня");
        }
        return ValidationResult.ok();
    }
}