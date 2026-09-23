package com.supporttickets.api;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.supporttickets.persistence.TicketRepository;
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
class TicketControllerSearchTest {

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
  void keywordMatchesTitleCaseInsensitive() throws Exception {
    createTicket("Network outage", "core switch");
    createTicket("Badge access", "door reader");

    mockMvc.perform(get("/api/v1/tickets").param("keyword", "NETWORK"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items", hasSize(1)))
        .andExpect(jsonPath("$.items[0].title").value("Network outage"));
  }

  @Test
  void keywordMatchesDescription() throws Exception {
    createTicket("Alpha", "fiber backbone down");
    createTicket("Beta", "printer jam");

    mockMvc.perform(get("/api/v1/tickets").param("keyword", "BACKBONE"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items", hasSize(1)))
        .andExpect(jsonPath("$.items[0].title").value("Alpha"));
  }

  @Test
  void statusFilterAlone() throws Exception {
    String openId = createTicket("Stay open", "x");
    String progressing = createTicket("Move me", "y");
    mockMvc.perform(post("/api/v1/tickets/{id}/status", progressing)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"status\":\"IN_PROGRESS\"}"))
        .andExpect(status().isOk());

    mockMvc.perform(get("/api/v1/tickets").param("status", "IN_PROGRESS"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items", hasSize(1)))
        .andExpect(jsonPath("$.items[0].id").value(progressing))
        .andExpect(jsonPath("$.items[0].id").value(org.hamcrest.Matchers.not(openId)));
  }

  @Test
  void statusFilterAndKeywordCombineWithAnd() throws Exception {
    String openId = createTicket("VPN issue", "remote access");
    mockMvc.perform(post("/api/v1/tickets/{id}/status", openId)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"status\":\"IN_PROGRESS\"}"))
        .andExpect(status().isOk());
    createTicket("VPN setup guide", "docs only");

    mockMvc.perform(get("/api/v1/tickets")
            .param("keyword", "VPN")
            .param("status", "IN_PROGRESS"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items", hasSize(1)))
        .andExpect(jsonPath("$.items[0].status").value("IN_PROGRESS"))
        .andExpect(jsonPath("$.items[0].id").value(openId));
  }

  @Test
  void emptyKeywordDoesNotConstrainResults() throws Exception {
    createTicket("One", "a");
    createTicket("Two", "b");

    mockMvc.perform(get("/api/v1/tickets").param("keyword", ""))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items", hasSize(2)));
  }

  @Test
  void invalidStatusQueryReturns400() throws Exception {
    mockMvc.perform(get("/api/v1/tickets").param("status", "NOPE"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
  }

  @Test
  void noMatchesReturnsEmptyItems() throws Exception {
    createTicket("Alpha", "one");
    mockMvc.perform(get("/api/v1/tickets").param("keyword", "zzz-none"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items", hasSize(0)));
  }

  @Test
  void filteredListPreservesNewestFirstOrder() throws Exception {
    String older = createTicket("Shared older", "shared-token");
    Thread.sleep(15);
    String newer = createTicket("Shared newer", "shared-token");

    mockMvc.perform(get("/api/v1/tickets").param("keyword", "shared-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items", hasSize(2)))
        .andExpect(jsonPath("$.items[0].id").value(newer))
        .andExpect(jsonPath("$.items[1].id").value(older));
  }

  private String createTicket(String title, String description) throws Exception {
    MvcResult result = mockMvc.perform(post("/api/v1/tickets")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"title":"%s","description":"%s","priority":"MEDIUM"}
                """.formatted(title, description)))
        .andExpect(status().isCreated())
        .andReturn();
    JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
    return body.get("id").asText();
  }
}
