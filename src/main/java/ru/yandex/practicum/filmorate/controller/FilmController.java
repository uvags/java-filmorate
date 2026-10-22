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
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.validator.FilmValidator;
import ru.yandex.practicum.filmorate.validator.ValidationResult;

@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();
    private final FilmValidator filmValidator = new FilmValidator();
    private static final Logger log = LoggerFactory.getLogger(FilmController.class);

    @GetMapping
    public List<Film> findAll() {
        return new ArrayList<>(films.values());
    }

    @PostMapping
    public Film addFilm(@RequestBody Film film) {
        ValidationResult result = filmValidator.validate(film);
        if (!result.isValid()) {
            log.warn("Film не прошёл валидацию: {}", result.getMessage());
            throw new ValidationException(result.getMessage());
        }
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Добавлен Film: {}", film);
        return film;
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film film) {
        ValidationResult result = filmValidator.validate(film);
        if (!result.isValid()) {
            log.warn("Film не прошёл валидацию: {}", result.getMessage());
            throw new ValidationException(result.getMessage());
        }
        if (film.getId() == null || !films.containsKey(film.getId())) {
            log.warn("Film с id {} не найден", film.getId());
            throw new NotFoundException("Film не найден");
        }
        films.put(film.getId(), film);
        log.info("Обновлён Film: {}", film);
        return film;
    }

    private Long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}