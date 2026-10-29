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
public class JdbcMessageRepository {
    private static final String SELECT_FIELDS = """
            SELECT m.id, m.content, m.created_at,
                   u.id AS user_id, u.username, u.display_name
            FROM messages m JOIN users u ON u.id = m.user_id
            """;

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Message> rowMapper = (rs, rowNum) -> new Message(
            rs.getLong("id"),
            rs.getString("content"),
            new UserAccount(rs.getLong("user_id"), rs.getString("username"), rs.getString("display_name")),
            rs.getTimestamp("created_at").toLocalDateTime());

    public JdbcMessageRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Message save(String content, UserAccount author) {
        var keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO messages (content, user_id) VALUES (?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, content);
            statement.setLong(2, author.getId());
            return statement;
        }, keyHolder);
        Number key = keyHolder.getKeys() == null ? null : (Number) keyHolder.getKeys().get("id");
        if (key == null) throw new IllegalStateException("Database did not return a message ID");
        return findById(key.longValue()).orElseThrow();
    }

    public int saveAll(List<CreateMessageRequest> requests) {
        int[][] result = jdbcTemplate.batchUpdate(
                "INSERT INTO messages (content, user_id) VALUES (?, ?)",
                requests,
                Math.min(100, requests.size()),
                (statement, request) -> {
                    statement.setString(1, request.content().trim());
                    statement.setLong(2, request.userId());
                });
        int inserted = 0;
        for (int[] batch : result) inserted += batch.length;
        return inserted;
    }

    public Optional<Message> findById(long id) {
        List<Message> messages = jdbcTemplate.query(
                SELECT_FIELDS + " WHERE m.id = ?", rowMapper, id);
        return messages.stream().findFirst();
    }

    public List<Message> findPage(int page, int size) {
        return jdbcTemplate.query(
                SELECT_FIELDS + " ORDER BY m.created_at DESC, m.id DESC LIMIT ? OFFSET ?",
                rowMapper, size, page * size);
    }

    public List<Message> findByAuthor(String author, int page, int size) {
        String pattern = "%" + author.toLowerCase() + "%";
        return jdbcTemplate.query(
                SELECT_FIELDS + " WHERE LOWER(u.username) LIKE ? OR LOWER(u.display_name) LIKE ? " +
                        "ORDER BY m.created_at DESC, m.id DESC LIMIT ? OFFSET ?",
                rowMapper, pattern, pattern, size, page * size);
    }

    public void clear() {
        jdbcTemplate.update("DELETE FROM messages");
    }
}
