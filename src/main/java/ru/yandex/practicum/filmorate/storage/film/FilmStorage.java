package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;
import java.util.List;

public interface FilmStorage {
    Film add(Film film);
    Film getById(Long id);
    List<Film> findAll();
    Film update(Film film);
    void delete(Long id);
}