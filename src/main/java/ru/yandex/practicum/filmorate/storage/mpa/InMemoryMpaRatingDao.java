package ru.yandex.practicum.filmorate.storage.mpa;

import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.Optional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryMpaRatingDao implements MpaRatingDao {
    private final Map<Long, MpaRating> ratings = new HashMap<>();

    public InMemoryMpaRatingDao() {
        ratings.put(1L, new MpaRating(1L, "G"));
        ratings.put(2L, new MpaRating(2L, "PG"));
        ratings.put(3L, new MpaRating(3L, "PG-13"));
        ratings.put(4L, new MpaRating(4L, "R"));
        ratings.put(5L, new MpaRating(5L, "NC-17"));
    }

    @Override
    public Optional<MpaRating> findById(Long id) {
        return Optional.ofNullable(ratings.get(id));
    }

    @Override
    public List<MpaRating> findAll() {
        return List.copyOf(ratings.values());
    }

}