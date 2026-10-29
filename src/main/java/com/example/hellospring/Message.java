package com.example.hellospring;

import java.time.LocalDateTime;

public class Message {
    private Long id;
    private String content;
    private UserAccount author;
    private LocalDateTime createdAt;

    public Message() {
    }

    public Message(Long id, String content, UserAccount author, LocalDateTime createdAt) {
        this.id = id;
        this.content = content;
        this.author = author;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public UserAccount getAuthor() { return author; }
    public void setAuthor(UserAccount author) { this.author = author; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
