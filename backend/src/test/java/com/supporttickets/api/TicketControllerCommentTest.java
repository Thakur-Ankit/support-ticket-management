package com.supporttickets.api;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.supporttickets.persistence.CommentRepository;
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
class TicketControllerCommentTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private TicketRepository ticketRepository;

  @Autowired
  private CommentRepository commentRepository;

  @Autowired
  private ObjectMapper objectMapper;

  @BeforeEach
  void clean() {
    commentRepository.deleteAll();
    ticketRepository.deleteAll();
  }

  @Test
  void addCommentOnOpenReturns201() throws Exception {
    String ticketId = createTicket();

    mockMvc.perform(post("/api/v1/tickets/{id}/comments", ticketId)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"content":"Looking into it","author":"ops@example.com"}
                """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.content").value("Looking into it"))
        .andExpect(jsonPath("$.author").value("ops@example.com"))
        .andExpect(jsonPath("$.id").isNotEmpty())
        .andExpect(jsonPath("$.createdAt").isNotEmpty());
  }

  @Test
  void getDetailReturnsCommentsOldestFirst() throws Exception {
    String ticketId = createTicket();

    mockMvc.perform(post("/api/v1/tickets/{id}/comments", ticketId)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"content\":\"First\",\"author\":\"a@b.co\"}"))
        .andExpect(status().isCreated());
    Thread.sleep(10);
    mockMvc.perform(post("/api/v1/tickets/{id}/comments", ticketId)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"content\":\"Second\",\"author\":\"a@b.co\"}"))
        .andExpect(status().isCreated());

    mockMvc.perform(get("/api/v1/tickets/{id}", ticketId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.comments", hasSize(2)))
        .andExpect(jsonPath("$.comments[0].content").value("First"))
        .andExpect(jsonPath("$.comments[1].content").value("Second"));
  }

  @Test
  void addCommentOnClosedReturns409AndDoesNotPersist() throws Exception {
    String ticketId = createTicket();
    closeTicket(ticketId);

    mockMvc.perform(post("/api/v1/tickets/{id}/comments", ticketId)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"content\":\"Too late\",\"author\":\"ops@example.com\"}"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.error").value("COMMENT_NOT_ALLOWED"));

    mockMvc.perform(get("/api/v1/tickets/{id}", ticketId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.comments", hasSize(0)));
  }

  @Test
  void addCommentOnCancelledReturns409() throws Exception {
    String ticketId = createTicket();
    mockMvc.perform(post("/api/v1/tickets/{id}/status", ticketId)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"status\":\"CANCELLED\"}"))
        .andExpect(status().isOk());

    mockMvc.perform(post("/api/v1/tickets/{id}/comments", ticketId)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"content\":\"Nope\",\"author\":\"ops@example.com\"}"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.error").value("COMMENT_NOT_ALLOWED"));
  }

  @Test
  void addCommentOnResolvedSucceeds() throws Exception {
    String ticketId = createTicket();
    mockMvc.perform(post("/api/v1/tickets/{id}/status", ticketId)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"status\":\"IN_PROGRESS\"}")).andExpect(status().isOk());
    mockMvc.perform(post("/api/v1/tickets/{id}/status", ticketId)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"status\":\"RESOLVED\"}")).andExpect(status().isOk());

    mockMvc.perform(post("/api/v1/tickets/{id}/comments", ticketId)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"content\":\"Verified fix\",\"author\":\"qa@example.com\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.content").value("Verified fix"));
  }

  @Test
  void emptyContentReturns400() throws Exception {
    String ticketId = createTicket();

    mockMvc.perform(post("/api/v1/tickets/{id}/comments", ticketId)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"content\":\"   \",\"author\":\"ops@example.com\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
  }

  @Test
  void missingAuthorReturns400() throws Exception {
    String ticketId = createTicket();

    mockMvc.perform(post("/api/v1/tickets/{id}/comments", ticketId)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"content\":\"Has content\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
  }

  @Test
  void unknownTicketReturns404() throws Exception {
    mockMvc.perform(post("/api/v1/tickets/{id}/comments", UUID.randomUUID())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"content\":\"x\",\"author\":\"y\"}"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("NOT_FOUND"));
  }

  private String createTicket() throws Exception {
    MvcResult result = mockMvc.perform(post("/api/v1/tickets")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"title":"Comment target","description":"body","priority":"LOW"}
                """))
        .andExpect(status().isCreated())
        .andReturn();
    JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
    return body.get("id").asText();
  }

  private void closeTicket(String ticketId) throws Exception {
    mockMvc.perform(post("/api/v1/tickets/{id}/status", ticketId)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"status\":\"IN_PROGRESS\"}")).andExpect(status().isOk());
    mockMvc.perform(post("/api/v1/tickets/{id}/status", ticketId)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"status\":\"RESOLVED\"}")).andExpect(status().isOk());
    mockMvc.perform(post("/api/v1/tickets/{id}/status", ticketId)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"status\":\"CLOSED\"}")).andExpect(status().isOk());
  }
}
