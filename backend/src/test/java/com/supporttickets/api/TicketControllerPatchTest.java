package com.supporttickets.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.supporttickets.domain.TicketStatus;
import com.supporttickets.persistence.TicketEntity;
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
class TicketControllerPatchTest {

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
  void patchUpdatesFieldsWithoutChangingStatus() throws Exception {
    String id = createTicket();

    mockMvc.perform(patch("/api/v1/tickets/{id}", id)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "title": "Updated title",
                  "description": "Updated description",
                  "priority": "URGENT",
                  "assignee": "owner@example.com"
                }
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("Updated title"))
        .andExpect(jsonPath("$.description").value("Updated description"))
        .andExpect(jsonPath("$.priority").value("URGENT"))
        .andExpect(jsonPath("$.assignee").value("owner@example.com"))
        .andExpect(jsonPath("$.status").value("OPEN"));
  }

  @Test
  void patchOnClosedAllowsContentChange() throws Exception {
    String id = createTicket();
    mockMvc.perform(post("/api/v1/tickets/{id}/status", id)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"status\":\"IN_PROGRESS\"}")).andExpect(status().isOk());
    mockMvc.perform(post("/api/v1/tickets/{id}/status", id)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"status\":\"RESOLVED\"}")).andExpect(status().isOk());
    mockMvc.perform(post("/api/v1/tickets/{id}/status", id)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"status\":\"CLOSED\"}")).andExpect(status().isOk());

    mockMvc.perform(patch("/api/v1/tickets/{id}", id)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"title\":\"Still editable\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("Still editable"))
        .andExpect(jsonPath("$.status").value("CLOSED"));
  }

  @Test
  void patchNullAssigneeClearsAssignee() throws Exception {
    String id = createTicket();
    mockMvc.perform(patch("/api/v1/tickets/{id}", id)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"assignee\":\"a@b.co\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.assignee").value("a@b.co"));

    mockMvc.perform(patch("/api/v1/tickets/{id}", id)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"assignee\":null}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.assignee").value(nullValue()));
  }

  @Test
  void patchOmittingAssigneeLeavesAssigneeUnchanged() throws Exception {
    String id = createTicket();
    mockMvc.perform(patch("/api/v1/tickets/{id}", id)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"assignee\":\"keep@example.com\"}"))
        .andExpect(status().isOk());

    mockMvc.perform(patch("/api/v1/tickets/{id}", id)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"title\":\"Only title\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("Only title"))
        .andExpect(jsonPath("$.assignee").value("keep@example.com"));
  }

  @Test
  void patchRejectsStatusField() throws Exception {
    String id = createTicket();
    mockMvc.perform(patch("/api/v1/tickets/{id}", id)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"status\":\"CLOSED\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));

    assertThat(ticketRepository.findById(UUID.fromString(id))).get()
        .extracting(TicketEntity::getStatus)
        .isEqualTo(TicketStatus.OPEN);
  }

  private String createTicket() throws Exception {
    MvcResult result = mockMvc.perform(post("/api/v1/tickets")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"title":"Patch me","description":"body","priority":"LOW"}
                """))
        .andExpect(status().isCreated())
        .andReturn();
    JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
    return body.get("id").asText();
  }
}
