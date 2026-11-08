package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FilmRowMapper implements RowMapper<Film> {
    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getLong("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        Date rd = rs.getDate("release_date");
        film.setReleaseDate(rd != null ? rd.toLocalDate() : null);
        film.setDuration(rs.getInt("duration"));
        Long ratingId = rs.getObject("rating_id", Long.class);
        if (ratingId != null) {
            film.setMpa(new MpaRating(ratingId, rs.getString("mpa_name")));
        }
        return film;
    }
}