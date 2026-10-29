package com.example.hellospring;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MessageService {
    private static final int MAX_PAGE_SIZE = 100;
    private final JdbcMessageRepository messageRepository;
    private final UserRepository userRepository;

    public MessageService(JdbcMessageRepository messageRepository, UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    public Message getById(long id) {
        return messageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Message " + id + " not found"));
    }

    public List<Message> getPage(int page, int size) {
        validatePage(page, size);
        return messageRepository.findPage(page, size);
    }

    public List<Message> findByAuthor(String author, int page, int size) {
        validatePage(page, size);
        if (author == null || author.isBlank()) throw new IllegalArgumentException("Author must not be empty");
        return messageRepository.findByAuthor(author.trim(), page, size);
    }

    @Transactional
    public Message create(CreateMessageRequest request) {
        validateRequest(request);
        UserAccount user = requireUser(request.userId());
        return messageRepository.save(request.content().trim(), user);
    }

    @Transactional
    public int createBatch(List<CreateMessageRequest> requests) {
        if (requests == null || requests.isEmpty()) throw new IllegalArgumentException("Batch must not be empty");
        if (requests.size() > 1000) throw new IllegalArgumentException("Batch size must not exceed 1000");
        for (CreateMessageRequest request : requests) {
            validateRequest(request);
            requireUser(request.userId());
        }
        return messageRepository.saveAll(requests);
    }

    private void validateRequest(CreateMessageRequest request) {
        if (request == null || request.content() == null || request.content().isBlank()) {
            throw new IllegalArgumentException("Message content must not be empty");
        }
        if (request.userId() == null) throw new IllegalArgumentException("userId is required");
    }

    private UserAccount requireUser(long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User " + id + " not found"));
    }

    private void validatePage(int page, int size) {
        if (page < 0) throw new IllegalArgumentException("page must be non-negative");
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("size must be between 1 and " + MAX_PAGE_SIZE);
        }
    }
}
