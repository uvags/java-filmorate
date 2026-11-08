package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.MpaRating;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MpaRatingDbStorage implements MpaRatingDao {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<MpaRating> findAll() {
        return jdbcTemplate.query(
                "SELECT id, name FROM mpa_ratings ORDER BY id", new MpaRatingRowMapper());
    }

    @Override
    public Optional<MpaRating> findById(Long id) {
        return jdbcTemplate.query("SELECT id, name FROM mpa_ratings WHERE id = ?", new MpaRatingRowMapper(), id)
                .stream()
                .findFirst();
    }
}