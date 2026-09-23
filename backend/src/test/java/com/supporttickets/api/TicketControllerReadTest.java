package com.supporttickets.api;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.supporttickets.persistence.TicketRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TicketControllerReadTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private TicketRepository ticketRepository;

  @Autowired
  private ObjectMapper objectMapper;

  @BeforeEach
  void clean() {
    ticketRepository.deleteAll();
  }

  @Test
  void listReturnsNewestCreatedFirst() throws Exception {
    String olderId = createTicket("Older", "first");
    Thread.sleep(5);
    String newerId = createTicket("Newer", "second");

    mockMvc.perform(get("/api/v1/tickets"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items", hasSize(2)))
        .andExpect(jsonPath("$.items[0].id").value(newerId))
        .andExpect(jsonPath("$.items[1].id").value(olderId))
        .andExpect(jsonPath("$.items[*].title", contains("Newer", "Older")));
  }

  @Test
  void getReturnsDetail() throws Exception {
    String id = createTicket("Detail me", "full body");

    mockMvc.perform(get("/api/v1/tickets/{id}", id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(id))
        .andExpect(jsonPath("$.title").value("Detail me"))
        .andExpect(jsonPath("$.description").value("full body"))
        .andExpect(jsonPath("$.comments").isArray());
  }

  @Test
  void getUnknownReturns404() throws Exception {
    mockMvc.perform(get("/api/v1/tickets/{id}", UUID.randomUUID()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("NOT_FOUND"));
  }

  private String createTicket(String title, String description) throws Exception {
    MvcResult result = mockMvc.perform(post("/api/v1/tickets")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"title":"%s","description":"%s","priority":"LOW"}
                """.formatted(title, description)))
        .andExpect(status().isCreated())
        .andReturn();
    JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
    return body.get("id").asText();
  }
}
