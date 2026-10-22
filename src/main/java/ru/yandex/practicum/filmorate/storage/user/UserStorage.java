package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;
import java.util.List;

public interface UserStorage {
    User getById(Long id);
    List<User> findAll();
    User add(User user);
    User update(User user);
    void delete(Long id);
}