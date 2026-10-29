package com.example.hellospring;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class HelloSpringApplicationTests {
    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcMessageRepository messageRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private MessageService messageService;
    private UserAccount arseniy;
    private UserAccount maria;

    @BeforeEach
    void resetDatabase() {
        messageRepository.clear();
        userRepository.clear();
        arseniy = userRepository.save(new UserAccount("arseniy", "Арсений Паниклов"));
        maria = userRepository.save(new UserAccount("maria", "Мария"));
    }

    @Test
    void createsMessageAndReadsItById() throws Exception {
        mockMvc.perform(post("/api/messages").contextPath("/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Привет, JDBC!\",\"userId\":" + arseniy.getId() + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.author.username").value("arseniy"));

        long id = messageRepository.findPage(0, 1).get(0).getId();
        mockMvc.perform(get("/api/messages/{id}", id).contextPath("/api"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Привет, JDBC!"));
    }

    @Test
    void returns404ForMissingMessage() throws Exception {
        mockMvc.perform(get("/api/messages/999999").contextPath("/api"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Message 999999 not found"));
    }

    @Test
    void paginatesMessages() throws Exception {
        for (int i = 1; i <= 5; i++) {
            messageService.create(new CreateMessageRequest("Сообщение " + i, arseniy.getId()));
        }
        mockMvc.perform(get("/api/messages?page=1&size=2").contextPath("/api"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].content").value("Сообщение 3"));
    }

    @Test
    void createsBatchInOneRequest() throws Exception {
        String json = """
                [
                  {"content":"Первое","userId":%d},
                  {"content":"Второе","userId":%d}
                ]
                """.formatted(arseniy.getId(), maria.getId());
        mockMvc.perform(post("/api/messages/batch").contextPath("/api")
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.created").value(2));
    }

    @Test
    void batchIsAtomicWhenUserDoesNotExist() throws Exception {
        try {
            messageService.createBatch(List.of(
                    new CreateMessageRequest("Валидное", arseniy.getId()),
                    new CreateMessageRequest("Ошибка", 999999L)));
        } catch (ResourceNotFoundException ignored) {
        }
        org.junit.jupiter.api.Assertions.assertTrue(messageRepository.findPage(0, 10).isEmpty());
    }

    @Test
    void searchesMessagesByAuthor() throws Exception {
        messageService.create(new CreateMessageRequest("От Арсения", arseniy.getId()));
        messageService.create(new CreateMessageRequest("От Марии", maria.getId()));
        mockMvc.perform(get("/api/messages/search?author=паниклов").contextPath("/api"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].author.username").value("arseniy"));
    }

    @Test
    void createsAndListsUsers() throws Exception {
        mockMvc.perform(post("/api/users").contextPath("/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"ivan\",\"displayName\":\"Иван\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber());
        mockMvc.perform(get("/api/users").contextPath("/api"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void validatesPaginationAndPayload() throws Exception {
        mockMvc.perform(get("/api/messages?page=-1&size=30").contextPath("/api"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/messages").contextPath("/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\" \",\"userId\":" + arseniy.getId() + "}"))
                .andExpect(status().isBadRequest());
    }
}
