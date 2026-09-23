package com.supporttickets.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.supporttickets.domain.IllegalTransitionException;
import com.supporttickets.domain.Priority;
import com.supporttickets.domain.TicketStatus;
import com.supporttickets.persistence.TicketEntity;
import com.supporttickets.persistence.TicketRepository;
import com.supporttickets.service.TicketService;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TicketLifecycleIntegrationTest {

  @Autowired
  private TicketService ticketService;

  @Autowired
  private TicketRepository ticketRepository;

  @BeforeEach
  void clean() {
    ticketRepository.deleteAll();
  }

  @Test
  void happyPathOpenToInProgressToResolvedToClosed() {
    UUID id = seedTicket(TicketStatus.OPEN);

    assertThat(ticketService.changeStatus(id, TicketStatus.IN_PROGRESS).status())
        .isEqualTo(TicketStatus.IN_PROGRESS);
    assertThat(ticketService.changeStatus(id, TicketStatus.RESOLVED).status())
        .isEqualTo(TicketStatus.RESOLVED);
    assertThat(ticketService.changeStatus(id, TicketStatus.CLOSED).status())
        .isEqualTo(TicketStatus.CLOSED);

    assertThat(ticketRepository.findById(id)).get()
        .extracting(TicketEntity::getStatus)
        .isEqualTo(TicketStatus.CLOSED);
  }

  @Test
  void cancelFromOpen() {
    UUID id = seedTicket(TicketStatus.OPEN);
    assertThat(ticketService.changeStatus(id, TicketStatus.CANCELLED).status())
        .isEqualTo(TicketStatus.CANCELLED);
  }

  @Test
  void cancelFromInProgress() {
    UUID id = seedTicket(TicketStatus.OPEN);
    ticketService.changeStatus(id, TicketStatus.IN_PROGRESS);
    assertThat(ticketService.changeStatus(id, TicketStatus.CANCELLED).status())
        .isEqualTo(TicketStatus.CANCELLED);
  }

  @Test
  void illegalTransitionDoesNotPersist() {
    UUID id = seedTicket(TicketStatus.RESOLVED);

    assertThatThrownBy(() -> ticketService.changeStatus(id, TicketStatus.OPEN))
        .isInstanceOf(IllegalTransitionException.class);

    assertThat(ticketRepository.findById(id)).get()
        .extracting(TicketEntity::getStatus)
        .isEqualTo(TicketStatus.RESOLVED);
  }

  private UUID seedTicket(TicketStatus status) {
    TicketEntity ticket = new TicketEntity();
    ticket.setTitle("Lifecycle test");
    ticket.setDescription("Seed for lifecycle integration");
    ticket.setPriority(Priority.HIGH);
    ticket.setStatus(status);
    return ticketRepository.save(ticket).getId();
  }
}
