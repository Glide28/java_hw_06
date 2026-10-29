package com.example.hellospring;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping
    public List<UserAccount> getUsers() {
        return service.getAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserAccount createUser(@RequestBody UserAccount user) {
        return service.create(user);
    }
}
