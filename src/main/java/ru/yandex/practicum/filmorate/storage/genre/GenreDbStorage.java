package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GenreDbStorage implements GenreDao {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Genre> findAll() {
        return jdbcTemplate.query(
                "SELECT id, name FROM genres ORDER BY id", new GenreRowMapper());
    }

    @Override
    public Optional<Genre> findById(Long id) {
        return jdbcTemplate.query("SELECT id, name FROM genres WHERE id = ?", new GenreRowMapper(), id)
                .stream()
                .findFirst();
    }
}