package ru.yandex.practicum.filmorate.controller;

public class ErrorResponse {
    // краткое название ошибки
    private final String error;
    // подробное описание
    private final String description;

    public ErrorResponse(String error, String description) {
        this.error = error;
        this.description = description;
    }

    public String getError() {
        return error;
    }

    public String getDescription() {
        return description;
    }
}