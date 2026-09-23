package com.supporttickets.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
class TicketControllerCreateTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private TicketRepository ticketRepository;

  @BeforeEach
  void clean() {
    ticketRepository.deleteAll();
  }

  @Test
  void createReturns201LocationAndOpenStatus() throws Exception {
    MvcResult result = mockMvc.perform(post("/api/v1/tickets")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "title": "Printer jam",
                  "description": "Floor 2 printer stuck",
                  "priority": "HIGH",
                  "assignee": "ops@example.com"
                }
                """))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", org.hamcrest.Matchers.matchesPattern("/api/v1/tickets/.+")))
        .andExpect(jsonPath("$.title").value("Printer jam"))
        .andExpect(jsonPath("$.status").value("OPEN"))
        .andExpect(jsonPath("$.priority").value("HIGH"))
        .andExpect(jsonPath("$.assignee").value("ops@example.com"))
        .andReturn();

    String id = result.getResponse().getHeader("Location").replace("/api/v1/tickets/", "");
    assertThat(ticketRepository.findById(UUID.fromString(id))).isPresent();
  }

  @Test
  void createRejectsMissingTitle() throws Exception {
    mockMvc.perform(post("/api/v1/tickets")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"description":"x","priority":"LOW"}
                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
  }

  @Test
  void createRejectsWhitespaceOnlyDescription() throws Exception {
    mockMvc.perform(post("/api/v1/tickets")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"title":"Ok","description":"   ","priority":"LOW"}
                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
  }

  @Test
  void createRejectsTitleLongerThan100AfterTrim() throws Exception {
    String title = "a".repeat(101);
    mockMvc.perform(post("/api/v1/tickets")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"title":"%s","description":"desc","priority":"MEDIUM"}
                """.formatted(title)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
        .andExpect(jsonPath("$.details[0].field").value("title"));
  }

  @Test
  void createRejectsClientStatusField() throws Exception {
    mockMvc.perform(post("/api/v1/tickets")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"title":"x","description":"y","priority":"LOW","status":"CLOSED"}
                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
  }

  @Test
  void createAllowsMissingAssignee() throws Exception {
    mockMvc.perform(post("/api/v1/tickets")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"title":"No assignee","description":"desc","priority":"URGENT"}
                """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.assignee").value(nullValue()));
  }
}
