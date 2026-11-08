package ru.yandex.practicum.filmorate.storage.mpa;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.MpaRating;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MpaRatingRowMapper implements RowMapper<MpaRating> {
    @Override
    public MpaRating mapRow(ResultSet rs, int rowNum) throws SQLException {
        MpaRating m = new MpaRating();
        m.setId(rs.getLong("id"));
        m.setName(rs.getString("name"));
        return m;
    }
}