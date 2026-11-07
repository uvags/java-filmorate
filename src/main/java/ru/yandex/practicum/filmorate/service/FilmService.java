package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.validator.ValidationResult;
import ru.yandex.practicum.filmorate.validator.Validator;
import java.util.*;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreService genreService;
    private final MpaRatingService mpaRatingService;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage, GenreService genreService,
                       MpaRatingService mpaRatingService) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.genreService = genreService;
        this.mpaRatingService = mpaRatingService;
    }

    public Film getById(Long id) {
        return fillMpa(filmStorage.getById(id),
                genreService.findAllMap(), mpaRatingService.findAllMap());
    }

    public List<Film> findAll() {
        Map<Long, Genre> genres = genreService.findAllMap();
        Map<Long, MpaRating> mpa = mpaRatingService.findAllMap();
        return filmStorage
                .findAll()
                .stream()
                .map(f -> fillMpa(f, genres, mpa))
                .toList();
    }

    public Film create(Film film) {
        validate(film);
        Map<Long, Genre> genres = genreService.findAllMap();
        Map<Long, MpaRating> mpa = mpaRatingService.findAllMap();
        checkMpaAndGenresExist(film, genres, mpa);
        Film saved = fillMpa(filmStorage.add(film), genres, mpa);
        log.info("Создан Film: id={}, name=\"{}\"", saved.getId(), saved.getName());
        return saved;
    }

    public Film update(Film film) {
        validate(film);
        Map<Long, Genre> genres = genreService.findAllMap();
        Map<Long, MpaRating> mpa = mpaRatingService.findAllMap();
        checkMpaAndGenresExist(film, genres, mpa);

        Film existing = filmStorage.getById(film.getId());
        film.setLikes(existing.getLikes());
        if (film.getGenres() == null) film.setGenres(existing.getGenres());

        Film updated = fillMpa(filmStorage.update(film), genres, mpa);
        log.info("Обновлён Film: id={}, name=\"{}\"", updated.getId(), updated.getName());
        return updated;
    }

    public void addLike(Long filmId, Long userId) {
        filmStorage.getById(filmId);
        userStorage.getById(userId);
        filmStorage.addLike(filmId, userId);
    }

    public void removeLike(Long filmId, Long userId) {
        filmStorage.getById(filmId);
        userStorage.getById(userId);
        filmStorage.removeLike(filmId, userId);
    }

    public List<Film> getPopular(int count) {
        if (count <= 0) {
            throw new ValidationException("Параметр count должен быть положительным целым числом");
        }
        Map<Long, Genre> genres = genreService.findAllMap();
        Map<Long, MpaRating> mpa = mpaRatingService.findAllMap();
        return filmStorage.
                findPopular(count).stream()
                .map(f -> fillMpa(f, genres, mpa))
                .toList();
    }

    private void validate(Film film) {
        ValidationResult result = Validator.validate(film);
        if (!result.isValid()) {
            log.warn("Film не валиден: name='{}', reason: {}", film.getName(), result.getMessage());
            throw new ValidationException(result.getMessage());
        }
    }

    // FK
    private void checkMpaAndGenresExist(Film film, Map<Long, Genre> genresById, Map<Long, MpaRating> mpaById) {
        if (film.getMpa() != null && film.getMpa().getId() != null && !mpaById.containsKey(film.getMpa().getId())) {
            log.warn("Не найден MPA с id={} при сохранении Film name='{}'", film.getMpa().getId(), film.getName());
            throw new NotFoundException("Рейтинг с id=" + film.getMpa().getId() + " не найден");
        }

        if (film.getGenres() != null) {
            for (Genre g : film.getGenres()) {
                if (g.getId() != null && !genresById.containsKey(g.getId())) {
                    log.warn("Не найден Genre с id={} при сохранении Film name='{}'", g.getId(), film.getName());
                    throw new NotFoundException("Жанр с id=" + g.getId() + " не найден");
                }
            }
        }
    }

    // дозаполнение данными
    private Film fillMpa(Film film, Map<Long, Genre> genresById, Map<Long, MpaRating> mpaById) {
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Long> seen = new LinkedHashSet<>();
            List<Genre> full = new ArrayList<>();
            for (Genre g : film.getGenres()) {
                if (!seen.add(g.getId())) continue;
                Genre found = genresById.get(g.getId());
                if (found != null) full.add(found);
            }
            film.setGenres(full);
        } else {
            film.setGenres(new ArrayList<>());
        }
        if (film.getMpa() != null && film.getMpa().getId() != null) {
            MpaRating found = mpaById.get(film.getMpa().getId());
            if (found != null) film.setMpa(found);
        }
        return film;
    }
}