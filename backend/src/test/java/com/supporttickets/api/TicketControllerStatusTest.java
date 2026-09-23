package com.supporttickets.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.supporttickets.domain.Priority;
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
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TicketControllerStatusTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private TicketRepository ticketRepository;

  @BeforeEach
  void clean() {
    ticketRepository.deleteAll();
  }

  @Test
  void openToInProgressReturnsUpdatedStatus() throws Exception {
    UUID id = seedTicket(TicketStatus.OPEN);

    mockMvc.perform(post("/api/v1/tickets/{id}/status", id)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"status\":\"IN_PROGRESS\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

    assertThat(ticketRepository.findById(id)).get()
        .extracting(TicketEntity::getStatus)
        .isEqualTo(TicketStatus.IN_PROGRESS);
  }

  @Test
  void closedToOpenReturns409AndLeavesStatusUnchanged() throws Exception {
    UUID id = seedTicket(TicketStatus.CLOSED);

    mockMvc.perform(post("/api/v1/tickets/{id}/status", id)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"status\":\"OPEN\"}"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.error").value("ILLEGAL_TRANSITION"))
        .andExpect(jsonPath("$.status").value(409));

    assertThat(ticketRepository.findById(id)).get()
        .extracting(TicketEntity::getStatus)
        .isEqualTo(TicketStatus.CLOSED);
  }

  @Test
  void resolvedToOpenReturns409() throws Exception {
    UUID id = seedTicket(TicketStatus.RESOLVED);

    mockMvc.perform(post("/api/v1/tickets/{id}/status", id)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"status\":\"OPEN\"}"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.error").value("ILLEGAL_TRANSITION"));

    assertThat(ticketRepository.findById(id)).get()
        .extracting(TicketEntity::getStatus)
        .isEqualTo(TicketStatus.RESOLVED);
  }

  @Test
  void cancelledToOpenReturns409() throws Exception {
    UUID id = seedTicket(TicketStatus.CANCELLED);

    mockMvc.perform(post("/api/v1/tickets/{id}/status", id)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"status\":\"OPEN\"}"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.error").value("ILLEGAL_TRANSITION"));

    assertThat(ticketRepository.findById(id)).get()
        .extracting(TicketEntity::getStatus)
        .isEqualTo(TicketStatus.CANCELLED);
  }

  @Test
  void unknownTicketReturns404() throws Exception {
    mockMvc.perform(post("/api/v1/tickets/{id}/status", UUID.randomUUID())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"status\":\"IN_PROGRESS\"}"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("NOT_FOUND"));
  }

  private UUID seedTicket(TicketStatus status) {
    TicketEntity ticket = new TicketEntity();
    ticket.setTitle("Status test");
    ticket.setDescription("Seed for status API tests");
    ticket.setPriority(Priority.MEDIUM);
    ticket.setStatus(status);
    return ticketRepository.save(ticket).getId();
  }
}
