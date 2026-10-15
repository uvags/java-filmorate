package ru.yandex.practicum.filmorate.validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.practicum.filmorate.model.Film;
import java.time.LocalDate;

public class FilmValidator {
    private static final Logger log = LoggerFactory.getLogger(FilmValidator.class);
    private static final int MAX_DESCRIPTION_LENGTH = 200;
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    public ValidationResult validate(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Ошибка валидации: пустое название фильма");
            return ValidationResult.error("Название фильма не может быть пустым");
        }
        if (film.getDescription() != null && film.getDescription().length() > MAX_DESCRIPTION_LENGTH) {
            log.warn("Ошибка валидации: описание фильма длиннее 200 символов");
            return ValidationResult.error("Максимальная длина описания фильма есть 200 символов");
        }
        if (film.getReleaseDate() == null) {
            log.warn("Ошибка валидации: пустая дата релиза фильма");
            return ValidationResult.error("Дата релиза фильма не может быть пустой");
        }
        if (film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            log.warn("Ошибка валидации: дата релиза фильма раньше 28 декабря 1895 года");
            return ValidationResult.error("Дата релиза фильма не может быть раньше 28 декабря 1895 года");
        }
        if (film.getDuration() <= 0) {
            log.warn("Ошибка валидации: продолжительность фильма меньше или равна 0 минут");
            return ValidationResult.error("Продолжительность фильма должна быть положительным числом");
        }
        return ValidationResult.ok();
    }
}