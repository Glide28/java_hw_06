package com.example.hellospring;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/messages")
public class MessageController {
    private final MessageService service;

    public MessageController(MessageService service) {
        this.service = service;
    }

    @GetMapping
    public List<Message> getMessages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {
        return service.getPage(page, size);
    }

    @GetMapping("/{id}")
    public Message getMessage(@PathVariable long id) {
        return service.getById(id);
    }

    @GetMapping("/search")
    public List<Message> searchByAuthor(
            @RequestParam String author,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {
        return service.findByAuthor(author, page, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Message createMessage(@RequestBody CreateMessageRequest request) {
        return service.create(request);
    }

    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Integer> createBatch(@RequestBody List<CreateMessageRequest> requests) {
        return Map.of("created", service.createBatch(requests));
    }
}
