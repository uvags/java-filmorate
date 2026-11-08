package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import java.time.LocalDate;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@Import(FilmDbStorage.class)
class FilmDbStorageTest {
    @Autowired private FilmDbStorage filmStorage;
    @Autowired private JdbcTemplate jdbc;

    private Film newFilm() {
        Film f = new Film();
        f.setName("Шрек");
        f.setDescription("desc");
        f.setReleaseDate(LocalDate.of(2001, 4, 22));
        f.setDuration(90);
        f.setMpa(new MpaRating(1L, null));
        return f;
    }

    @Test
    void shouldAddAndFindFilm() {
        Film saved = filmStorage.add(newFilm());

        assertThat(saved.getId()).isNotNull();

        Film found = filmStorage.getById(saved.getId());

        assertThat(found.getName()).isEqualTo("Шрек");
        assertThat(found.getMpa().getId()).isEqualTo(1L);
        assertThat(found.getMpa().getName()).isEqualTo("G");
    }

    @Test
    void shouldSaveGenres() {
        Film f = newFilm();
        f.setGenres(List.of(new Genre(1L, null), new Genre(2L, null)));
        Film saved = filmStorage.add(f);
        Film found = filmStorage.getById(saved.getId());

        assertThat(found.getGenres()).hasSize(2);
        assertThat(found
                .getGenres())
                .extracting(Genre::getName)
                .containsExactly("Комедия", "Драма");
    }

    @Test
    void shouldUpdateFilm() {
        Film saved = filmStorage.add(newFilm());
        saved.setName("Новое имя");
        filmStorage.update(saved);

        assertThat(filmStorage.getById(saved.getId()).getName()).isEqualTo("Новое имя");
    }

    @Test
    void shouldReturnPopularByLikes() {
        jdbc.update("INSERT INTO users (email, login, name, birthday) VALUES ('a@a.ru','a','A','2000-01-01')");
        jdbc.update("INSERT INTO users (email, login, name, birthday) VALUES ('b@b.ru','b','B','2000-01-01')");
        long u1 = jdbc.queryForObject("SELECT id FROM users WHERE login='a'", Long.class);
        long u2 = jdbc.queryForObject("SELECT id FROM users WHERE login='b'", Long.class);

        Film f1 = filmStorage.add(newFilm());
        Film f2 = filmStorage.add(newFilm());
        filmStorage.addLike(f1.getId(), u1);
        filmStorage.addLike(f1.getId(), u2);
        filmStorage.addLike(f2.getId(), u1);

        List<Film> popular = filmStorage.findPopular(10);
        assertThat(popular).hasSize(2);
        assertThat(popular.get(0).getId()).isEqualTo(f1.getId());
        assertThat(popular.get(1).getId()).isEqualTo(f2.getId());
    }

    @Test
    void shouldThrowWhenNotFound() {
        assertThatThrownBy(() -> filmStorage.getById(999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldDeleteFilm() {
        Film saved = filmStorage.add(newFilm());

        filmStorage.delete(saved.getId());

        assertThatThrownBy(() -> filmStorage.getById(saved.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldDeleteFilmCascadeGenres() {
        Film f = newFilm();
        f.setGenres(List.of(new Genre(1L, null), new Genre(2L, null)));
        Film saved = filmStorage.add(f);
        filmStorage.delete(saved.getId());
        Integer remaining = jdbc.queryForObject(
                "SELECT COUNT(*) FROM film_genres WHERE film_id = ?",
                Integer.class, saved.getId());

        assertThat(remaining).isZero();
    }

    @Test
    void shouldDeleteFilmCascadeLikes() {
        jdbc.update("INSERT INTO users (email, login, name, birthday) " +
                "VALUES ('a@a.ru','a','A','2000-01-01')");
        long userId = jdbc.queryForObject("SELECT id FROM users WHERE login='a'", Long.class);
        Film saved = filmStorage.add(newFilm());
        filmStorage.addLike(saved.getId(), userId);
        filmStorage.delete(saved.getId());
        Integer remaining = jdbc.queryForObject(
                "SELECT COUNT(*) FROM likes WHERE film_id = ?",
                Integer.class, saved.getId());

        assertThat(remaining).isZero();
    }

    @Test
    void shouldNotAffectOtherFilmsOnDelete() {
        Film toDelete = filmStorage.add(newFilm());
        Film toKeep = filmStorage.add(newFilm());
        filmStorage.delete(toDelete.getId());

        assertThat(filmStorage
                .findAll())
                .extracting(Film::getId)
                .containsExactly(toKeep.getId());
    }

    @Test
    void shouldThrowWhenDeleteNotFound() {
        assertThatThrownBy(() -> filmStorage.delete(999L))
                .isInstanceOf(NotFoundException.class);
    }
}