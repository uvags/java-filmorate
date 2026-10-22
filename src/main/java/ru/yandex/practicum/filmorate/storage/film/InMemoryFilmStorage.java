package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();
    private Long nextId = 0L;

    @Override
    public Film add(Film film) {
        film.setId(++nextId);
        films.put(film.getId(), film);
        log.info("Добавлен Film: id={}, name=\"{}\"", film.getId(), film.getName());
        return film;
    }

    @Override
    public Film update(Film film) {
        if (film.getId() == null || !films.containsKey(film.getId())) {
            log.warn("Попытка обновить данные несуществующего фильма: id={}", film.getId());
            throw new NotFoundException("Фильм с id=" + film.getId() + " не найден");
        }
        films.put(film.getId(), film);
        log.info("Обновлены данные Film: id={}, name=\"{}\"", film.getId(), film.getName());
        return film;
    }

    @Override
    public void delete(Long id) {
        Film removed = films.remove(id);
        if (removed != null) {
            log.info("Удалены данные Film: id={}, name=\"{}\"", id, removed.getName());
        }
    }

    @Override
    public Film getById(Long id) {
        Film film = films.get(id);
        if (film == null) {
            log.warn("Запрошенный Film не найден: id={}", id);
            throw new NotFoundException("Film с id=" + id + " не найден");
        }
        return film;
    }

    @Override
    public List<Film> findAll() {
        return new ArrayList<>(films.values());
    }
}