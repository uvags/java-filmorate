package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class InMemoryFilmStorageTest {
    private InMemoryFilmStorage storage;

    @BeforeEach
    void setUp() {
        storage = new InMemoryFilmStorage();
    }

    private Film film(String name) {
        Film f = new Film();
        f.setName(name);
        f.setDescription("description");
        f.setReleaseDate(LocalDate.of(2000, 1, 1));
        f.setDuration(100);

        return f;
    }

    @Test
    void addShouldAssignIncrementingIds() {
        assertEquals(1L, storage.add(film("A")).getId());
        assertEquals(2L, storage.add(film("B")).getId());
    }

    @Test
    void getByIdShouldReturnStoredFilm() {
        Film saved = storage.add(film("A"));

        assertEquals(saved, storage.getById(saved.getId()));
    }

    @Test
    void getByIdShouldThrowNotFoundExceptionWhenNotFound() {
        assertThrows(NotFoundException.class, () -> storage.getById(999L));
    }

    @Test
    void findAllShouldReturnAll() {
        storage.add(film("A"));
        storage.add(film("B"));

        assertEquals(2, storage.findAll().size());
    }

    @Test
    void findAllShouldReturnCopy() {
        storage.add(film("A"));
        List<Film> films = storage.findAll();
        films.clear();

        assertEquals(1, storage.findAll().size());
    }

    @Test
    void updateShouldReplaceExistingFilm() {
        Film saved = storage.add(film("A"));
        saved.setName("B");

        assertEquals("B", storage.update(saved).getName());
        assertEquals("B", storage.getById(saved.getId()).getName());
    }

    @Test
    void updateShouldThrowWhenUnknownFilm() {
        Film f = film("A");
        f.setId(999L);

        assertThrows(NotFoundException.class, () -> storage.update(f));
    }

    @Test
    void updateShouldThrowExceptionWhenNullId() {
        Film f = film("A");
        f.setId(null);

        assertThrows(NotFoundException.class, () -> storage.update(f));
    }

    @Test
    void deleteShouldRemove() {
        Film saved = storage.add(film("A"));
        storage.delete(saved.getId());

        assertThrows(NotFoundException.class, () -> storage.getById(saved.getId()));
    }

    @Test
    void deleteUnknownShouldNotThrow() {
        assertDoesNotThrow(() -> storage.delete(999L));
    }
}