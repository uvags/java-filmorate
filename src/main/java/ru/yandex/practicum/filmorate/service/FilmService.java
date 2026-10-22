package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.validator.ValidationResult;
import ru.yandex.practicum.filmorate.validator.Validator;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film getById(Long id) {
        return filmStorage.getById(id);
    }

    public List<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film create(Film film) {
        validate(film);
        return filmStorage.add(film);
    }

    public Film update(Film film) {
        validate(film);
        return filmStorage.update(film);
    }

    public void addLike(Long filmId, Long userId) {
        Film film = filmStorage.getById(filmId);
        userStorage.getById(userId); // проверка на то, есть ли такой User
        if (film.getLikes().add(userId)) {
            log.info("User userId={} лайкнул Film filmId={}, name =\"{}\"", userId, filmId, film.getName());
        } else {
            log.info("User userId={} лайкнул ранее Film filmId={}", userId, filmId);
        }
    }

    public void removeLike(Long filmId, Long userId) {
        Film film = filmStorage.getById(filmId);
        userStorage.getById(userId);
        if (film.getLikes().remove(userId)) {
            log.info("User userId={} удалил лайк у Film filmId={}, name =\"{}\"", userId, filmId, film.getName());
        } else {
            log.info("Film filmId={} не имеет лайка от User userId={}", filmId, userId);
        }
    }

    public List<Film> getPopular(int count) {
        if (count <= 0) {
            throw new ValidationException("Параметр count должен быть положительным целым числом");
        }
        return filmStorage.findAll().stream()
                .sorted((a, b) -> Integer.compare(b.getLikes().size(), a.getLikes().size()))
                .limit(count)
                .toList();
    }

    // используем утилитарный класс Validate, хотя, можно рассмотреть вариант с бином
    private void validate(Film film) {
        ValidationResult result = Validator.validate(film);
        if (!result.isValid()) {
            log.warn("Film не валиден: name='{}', reason: {}", film.getName(), result.getMessage());
            throw new ValidationException(result.getMessage());
        }
    }
}