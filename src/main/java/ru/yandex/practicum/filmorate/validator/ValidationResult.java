package ru.yandex.practicum.filmorate.validator;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public final class ValidationResult {
    private final boolean valid;
    private final String message;

    private ValidationResult(boolean valid, String message) {
        this.valid = valid;
        this.message = message;
    }

    public static ValidationResult ok() { return new ValidationResult(true, null); }
    public static ValidationResult error(String m) { return new ValidationResult(false, m); }
}