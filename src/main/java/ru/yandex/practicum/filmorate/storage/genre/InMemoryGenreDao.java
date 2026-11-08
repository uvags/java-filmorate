package ru.yandex.practicum.filmorate.storage.genre;

import ru.yandex.practicum.filmorate.model.Genre;
import java.util.Optional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryGenreDao implements GenreDao {
    private final Map<Long, Genre> genres = new HashMap<>();

    public InMemoryGenreDao() {
        genres.put(1L, new Genre(1L, "Комедия"));
        genres.put(2L, new Genre(2L, "Драма"));
        genres.put(3L, new Genre(3L, "Мультфильм"));
        genres.put(4L, new Genre(4L, "Триллер"));
        genres.put(5L, new Genre(5L, "Документальный"));
        genres.put(6L, new Genre(6L, "Боевик"));
    }

    @Override
    public Optional<Genre> findById(Long id) {
        return Optional.ofNullable(genres.get(id));
    }

    @Override
    public List<Genre> findAll() {
        return List.copyOf(genres.values());
    }
}