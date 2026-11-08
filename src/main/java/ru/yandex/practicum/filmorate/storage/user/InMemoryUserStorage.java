package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private Long nextId = 0L;

    @Override
    public User getById(Long id) {
        User user = users.get(id);
        if (user == null) {
            log.warn("User не найден: id={}", id);
            throw new NotFoundException("User с id=" + id + " не найден");
        }
        return user;
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User add(User user) {
        user.setId(++nextId);
        users.put(user.getId(), user);
        log.info("Добавлен User: id={}, login=\"{}\"", user.getId(), user.getLogin());
        return user;
    }

    @Override
    public User update(User user) {
        if (user.getId() == null || !users.containsKey(user.getId())) {
            log.warn("Попытка обновить данные несуществующего User: id={}", user.getId());
            throw new NotFoundException("Пользователь с id=" + user.getId() + " не найден");
        }
        users.put(user.getId(), user);
        log.info("Обновлены данные User: id={}, login=\"{}\"", user.getId(), user.getLogin());
        return user;
    }

    @Override
    public void delete(Long id) {
        User removed = users.remove(id);
        if (removed != null) {
            log.info("Удалены данные User: id={}, login=\"{}\"", id, removed.getLogin());
        }
    }
}