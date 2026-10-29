package com.example.hellospring;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public UserAccount create(UserAccount user) {
        if (user.getUsername() == null || user.getUsername().isBlank()) {
            throw new IllegalArgumentException("Username must not be empty");
        }
        if (user.getDisplayName() == null || user.getDisplayName().isBlank()) {
            throw new IllegalArgumentException("Display name must not be empty");
        }
        user.setId(null);
        user.setUsername(user.getUsername().trim());
        user.setDisplayName(user.getDisplayName().trim());
        return repository.save(user);
    }

    public List<UserAccount> getAll() {
        return repository.findAll();
    }
}
