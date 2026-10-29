package com.example.hellospring;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<UserAccount> rowMapper = (rs, rowNum) -> new UserAccount(
            rs.getLong("id"), rs.getString("username"), rs.getString("display_name"));

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public UserAccount save(UserAccount user) {
        var keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO users (username, display_name) VALUES (?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getDisplayName());
            return statement;
        }, keyHolder);
        Number key = keyHolder.getKeys() == null ? null : (Number) keyHolder.getKeys().get("id");
        if (key != null) user.setId(key.longValue());
        return user;
    }

    public Optional<UserAccount> findById(long id) {
        List<UserAccount> users = jdbcTemplate.query(
                "SELECT id, username, display_name FROM users WHERE id = ?", rowMapper, id);
        return users.stream().findFirst();
    }

    public List<UserAccount> findAll() {
        return jdbcTemplate.query(
                "SELECT id, username, display_name FROM users ORDER BY id", rowMapper);
    }

    public void clear() {
        jdbcTemplate.update("DELETE FROM users");
    }
}
