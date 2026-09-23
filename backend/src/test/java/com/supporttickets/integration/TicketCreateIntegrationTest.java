package com.supporttickets.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.supporttickets.api.dto.CreateTicketRequest;
import com.supporttickets.api.dto.TicketDetailResponse;
import com.supporttickets.domain.Priority;
import com.supporttickets.domain.TicketStatus;
import com.supporttickets.persistence.TicketRepository;
import com.supporttickets.service.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TicketCreateIntegrationTest {

  @Autowired
  private TicketService ticketService;

  @Autowired
  private TicketRepository ticketRepository;

  @BeforeEach
  void clean() {
    ticketRepository.deleteAll();
  }

  @Test
  void createPersistsOpenTicket() {
    TicketDetailResponse created = ticketService.create(
        new CreateTicketRequest("Persist me", "stored description", Priority.MEDIUM, null));

    assertThat(created.status()).isEqualTo(TicketStatus.OPEN);
    assertThat(ticketRepository.findById(created.id())).isPresent();
    assertThat(ticketRepository.findById(created.id())).get()
        .satisfies(entity -> {
          assertThat(entity.getTitle()).isEqualTo("Persist me");
          assertThat(entity.getStatus()).isEqualTo(TicketStatus.OPEN);
        });
  }
}
