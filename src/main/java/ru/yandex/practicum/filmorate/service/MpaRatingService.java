package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.mpa.MpaRatingDao;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MpaRatingService {
    private final MpaRatingDao mpaRatingDao;

    public MpaRating getById(Long id) {
        return mpaRatingDao
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Рейтинг с id=" + id + " не найден"));
    }

    public List<MpaRating> findAll() {
        return mpaRatingDao.findAll();
    }

    public Map<Long, MpaRating> findAllMap() {
        return mpaRatingDao
                .findAll()
                .stream()
                .collect(Collectors.toMap(MpaRating::getId, Function.identity()));
    }
}