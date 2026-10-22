package ru.yandex.practicum.filmorate.validator;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

public final class Validator {
    private static final int MAX_DESCRIPTION_LENGTH = 200;
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    private Validator() {
    }

    public static ValidationResult validate(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            return ValidationResult.error("Название фильма не может быть пустым");
        }
        if (film.getDescription() != null && film.getDescription().length() > MAX_DESCRIPTION_LENGTH) {
            return ValidationResult.error("Максимальная длина описания фильма есть 200 символов");
        }
        if (film.getReleaseDate() == null) {
            return ValidationResult.error("Дата релиза фильма не может быть пустой");
        }
        if (film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            return ValidationResult.error("Дата релиза фильма не может быть раньше 28 декабря 1895 года");
        }
        if (film.getDuration() <= 0) {
            return ValidationResult.error("Продолжительность фильма должна быть положительным числом");
        }
        return ValidationResult.ok();
    }

    public static ValidationResult validate(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            return ValidationResult.error("email не может быть пустым и должен содержать символ @");
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