package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository("filmDbStorage")
@Primary
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbc;
    private final FilmRowMapper mapper = new FilmRowMapper();

    public FilmDbStorage(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final String BASE_SELECT = """
            SELECT f.id,
            f.name,
            f.description,
            f.release_date,
            f.duration,
            f.rating_id,
            m.name AS mpa_name
            FROM films f
            LEFT JOIN mpa_ratings m ON m.id = f.rating_id
            """;

    @Override
    public Film getById(Long id) {
        List<Film> list = jdbc.query(BASE_SELECT + " WHERE f.id = ?", mapper, id);
        if (list.isEmpty()) {
            log.warn("Film не найден: id={}", id);
            throw new NotFoundException("Фильм с id=" + id + " не найден");
        }
        Film film = list.get(0);
        attachGenresAndLikes(List.of(film));
        return film;
    }

    @Override
    public List<Film> findAll() {
        List<Film> films = jdbc.query(BASE_SELECT + " ORDER BY f.id", mapper);
        attachGenresAndLikes(films);
        return films;
    }

    @Override
    @Transactional
    public Film add(Film film) {
        String sql = "INSERT INTO films (name, description, release_date, duration, rating_id) " +
                "VALUES (?, ?, ?, ?, ?)";
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            if (film.getMpa() != null && film.getMpa().getId() != null) {
                ps.setLong(5, film.getMpa().getId());
            } else {
                ps.setNull(5, Types.BIGINT);
            }
            return ps;
        }, kh);
        film.setId(kh.getKey().longValue());
        saveGenres(film);
        log.info("Добавлен Film: id={}, name=\"{}\"", film.getId(), film.getName());
        return getById(film.getId());
    }

    @Override
    @Transactional
    public Film update(Film film) {
        int rows = jdbc.update(
                "UPDATE films SET name=?, description=?, release_date=?, duration=?, rating_id=? WHERE id=?",
                film.getName(), film.getDescription(), Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa() != null ? film.getMpa().getId() : null,
                film.getId());
        if (rows == 0) {
            log.warn("Попытка обновить несуществующий Film: id={}", film.getId());
            throw new NotFoundException("Фильм с id=" + film.getId() + " не найден");
        }
        jdbc.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
        saveGenres(film);
        log.info("Обновлён Film: id={}, name=\"{}\"", film.getId(), film.getName());
        return getById(film.getId());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        jdbc.update("DELETE FROM film_genres WHERE film_id = ?", id);
        jdbc.update("DELETE FROM likes WHERE film_id = ?", id);
        int rows = jdbc.update("DELETE FROM films WHERE id = ?", id);
        if (rows == 0) {
            throw new NotFoundException("Фильм с id=" + id + " не найден");
        }
        log.info("Удалён Film: id={}", id);
    }

    @Override
    public List<Film> findPopular(int limit) {
        List<Long> ids = jdbc.queryForList(
                "SELECT f.id FROM films f " + "LEFT JOIN likes l ON l.film_id = f.id " +
                        "GROUP BY f.id " + "ORDER BY COUNT(l.user_id) DESC, f.id ASC LIMIT ?", Long.class, limit);
        if (ids.isEmpty()) return List.of();

        String in = ids.stream().map(x -> "?").collect(Collectors.joining(","));
        List<Film> films = jdbc.query(
                BASE_SELECT + " WHERE f.id IN (" + in + ")", mapper, ids.toArray());

        Map<Long, Film> byId = films.stream().collect(Collectors.toMap(Film::getId, f -> f));
        List<Film> result = ids.stream().map(byId::get).filter(Objects::nonNull).toList();
        attachGenresAndLikes(result);
        return result;
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        jdbc.update("MERGE INTO likes (film_id, user_id) KEY(film_id, user_id) VALUES (?, ?)",
                filmId, userId);
        log.info("User userId={} лайкнул Film filmId={}", userId, filmId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        jdbc.update("DELETE FROM likes WHERE film_id = ? AND user_id = ?", filmId, userId);
        log.info("User userId={} удалил лайк у Film filmId={}", userId, filmId);
    }

    private void saveGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) return;
        Set<Long> seen = new LinkedHashSet<>();
        for (Genre g : film.getGenres()) {
            if (g.getId() == null || !seen.add(g.getId())) continue;
            jdbc.update("MERGE INTO film_genres (film_id, genre_id) KEY(film_id, genre_id) VALUES (?, ?)",
                    film.getId(), g.getId());
        }
    }

    // против o(N) реквестов
    private void attachGenresAndLikes(List<Film> films) {
        if (films.isEmpty()) return;

        String ids = films.stream().map(f -> "?").collect(Collectors.joining(","));
        Object[] idArgs = films.stream().map(Film::getId).toArray();

        Map<Long, List<Genre>> genresByFilm = new HashMap<>();

        // gid - genres id, gname - это genres name
        jdbc.query(
                "SELECT fg.film_id, g.id AS gid, g.name AS gname " +
                        "FROM film_genres fg JOIN genres g ON g.id = fg.genre_id " +
                        "WHERE fg.film_id IN (" + ids + ") ORDER BY fg.film_id, g.id",
                rs -> {
                    long filmId = rs.getLong("film_id");
                    genresByFilm.computeIfAbsent(filmId, k -> new ArrayList<>())
                            .add(new Genre(rs.getLong("gid"), rs.getString("gname")));
                }, idArgs);

        Map<Long, Set<Long>> likesByFilm = new HashMap<>();
        jdbc.query("SELECT film_id, user_id FROM likes WHERE film_id IN (" + ids + ")",
                rs -> {
                    long filmId = rs.getLong("film_id");
                    long userId = rs.getLong("user_id");
                    likesByFilm.computeIfAbsent(filmId, k -> new HashSet<>()).add(userId);
                }, idArgs);

        for (Film f : films) {
            f.setGenres(genresByFilm.getOrDefault(f.getId(), new ArrayList<>()));
            f.getLikes().addAll(likesByFilm.getOrDefault(f.getId(), Set.of()));
        }
    }
}