package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository("userDbStorage")
@Primary
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbc;
    private final UserRowMapper mapper = new UserRowMapper(); // не @Component, используется только тут

    public UserDbStorage(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public User getById(Long id) {
        List<User> list = jdbc.query("SELECT * FROM users WHERE id = ?", mapper, id);
        if (list.isEmpty()) {
            log.warn("User не найден: id={}", id);
            throw new NotFoundException("User с id=" + id + " не найден");
        }
        User user = list.get(0);
        attachFriends(List.of(user));
        return user;
    }

    @Override
    public List<User> findAll() {
        List<User> users = jdbc.query("SELECT * FROM users ORDER BY id", mapper);
        attachFriends(users);
        return users;
    }

    @Override
    @Transactional
    public User add(User user) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)",
                    new String[]{"id"});
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            if (user.getBirthday() != null) ps.setDate(4, Date.valueOf(user.getBirthday()));
            else ps.setNull(4, Types.DATE);
            return ps;
        }, kh);
        user.setId(kh.getKey().longValue());
        log.info("Добавлен User: id={}, login=\"{}\"", user.getId(), user.getLogin());
        return user;
    }

    @Override
    @Transactional
    public User update(User user) {
        int rows = jdbc.update(
                "UPDATE users SET email=?, login=?, name=?, birthday=? WHERE id=?",
                user.getEmail(), user.getLogin(), user.getName(),
                user.getBirthday() != null ? Date.valueOf(user.getBirthday()) : null,
                user.getId());
        if (rows == 0) {
            log.warn("Попытка обновить несуществующего User: id={}", user.getId());
            throw new NotFoundException("Пользователь с id=" + user.getId() + " не найден");
        }
        log.info("Обновлён User: id={}, login=\"{}\"", user.getId(), user.getLogin());
        return user;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        jdbc.update("DELETE FROM likes       WHERE user_id = ?", id);
        jdbc.update("DELETE FROM friendships WHERE user_id = ? OR friend_id = ?", id, id);
        int rows = jdbc.update("DELETE FROM users WHERE id = ?", id);
        if (rows == 0) {
            throw new NotFoundException("Пользователь с id=" + id + " не найден");
        }
        log.info("Удалён User: id={}", id);
    }

    @Override
    public void addFriend(Long userId, Long friendId, FriendshipStatus status) {
        jdbc.update(
                "MERGE INTO friendships (user_id, friend_id, status) KEY(user_id, friend_id) VALUES (?, ?, ?)",
                userId, friendId, status.name());
        log.info("Сохранена дружба: userId={}, friendId={}, status={}", userId, friendId, status);
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        jdbc.update("DELETE FROM friendships WHERE user_id = ? AND friend_id = ?", userId, friendId);
        log.info("Удалена дружба: userId={}, friendId={}", userId, friendId);
    }

    @Override
    public Optional<FriendshipStatus> findFriendshipStatus(Long userId, Long friendId) {
        return jdbc.query(
                "SELECT status FROM friendships WHERE user_id = ? AND friend_id = ?",
                (rs, i) -> FriendshipStatus.valueOf(rs.getString("status")),
                userId, friendId
        ).stream().findFirst();
    }

    @Override
    public List<User> findFriends(Long userId) {
        return jdbc.query(
                "SELECT u.* FROM users u " +
                        "JOIN friendships f ON f.friend_id = u.id " +
                        "WHERE f.user_id = ? ORDER BY u.id",
                mapper, userId);
    }

    @Override
    public List<User> findCommonFriends(Long userId, Long otherId) {
        return jdbc.query(
                "SELECT u.* FROM users u " + "JOIN friendships f1 ON f1.friend_id = u.id AND f1.user_id = ? " +
                        "JOIN friendships f2 ON f2.friend_id = u.id AND f2.user_id = ? " + "ORDER BY u.id",
                mapper, userId, otherId);
    }

    private void attachFriends(List<User> users) {
        if (users.isEmpty()) return;
        String ids = users.stream().map(u -> "?").collect(Collectors.joining(","));
        Object[] idArgs = users.stream().map(User::getId).toArray();
        Map<Long, Set<Long>> friendsByUser = new HashMap<>();

//        List<Map<String, Object>> rows = jdbc.queryForList(
//          "SELECT user_id, friend_id FROM friendships WHERE user_id IN (" + ids + ")", idArgs);
//
//        for (Map<String, Object> row : rows) {
//            long u = ((Number) row.get("user_id")).longValue();
//            long f = ((Number) row.get("friend_id")).longValue();
//            Set<Long> set = friendsByUser.get(u);
//            if (set == null) {
//                set = new HashSet<>();
//                friendsByUser.put(u, set);
//            }
//            set.add(f);
//        }
        jdbc.query("SELECT user_id, friend_id FROM friendships WHERE user_id IN (" + ids + ")",
                rs -> {
                    long u = rs.getLong("user_id");
                    long f = rs.getLong("friend_id");
                    friendsByUser.computeIfAbsent(u, k -> new HashSet<>()).add(f);
                }, idArgs);

        for (User u : users) {
            u.getFriends().addAll(friendsByUser.getOrDefault(u.getId(), Set.of()));
        }
    }
}