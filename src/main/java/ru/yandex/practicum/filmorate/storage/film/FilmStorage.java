package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;
import java.util.List;

public interface FilmStorage {
    Film getById(Long id);

    List<Film> findAll();

    Film add(Film film);

    Film update(Film film);

    void delete(Long id);

    List<Film> findPopular(int limit);

    void addLike(Long filmId, Long userId);

    void removeLike(Long filmId, Long userId);
}