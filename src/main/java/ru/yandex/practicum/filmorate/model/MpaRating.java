package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class MpaRating {
    private Long id;
    private String name;

    public MpaRating() {}
}