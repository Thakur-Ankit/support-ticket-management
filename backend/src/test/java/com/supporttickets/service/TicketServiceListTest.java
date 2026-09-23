package com.supporttickets.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.supporttickets.api.dto.CreateTicketRequest;
import com.supporttickets.api.dto.TicketListResponse;
import com.supporttickets.api.dto.TicketSummaryResponse;
import com.supporttickets.domain.Priority;
import com.supporttickets.domain.TicketStatus;
import com.supporttickets.persistence.TicketRepository;
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
class TicketServiceListTest {

  @Autowired
  private TicketService ticketService;

  @Autowired
  private TicketRepository ticketRepository;

  @BeforeEach
  void clean() {
    ticketRepository.deleteAll();
  }

  @Test
  void blankKeywordTreatedAsNoKeywordConstraint() {
    ticketService.create(new CreateTicketRequest("One", "desc", Priority.LOW, null));
    ticketService.create(new CreateTicketRequest("Two", "desc", Priority.LOW, null));

    TicketListResponse blank = ticketService.list("   ", null);
    TicketListResponse omitted = ticketService.list(null, null);

    assertThat(blank.items()).hasSize(2);
    assertThat(omitted.items()).hasSize(2);
  }

  @Test
  void listAppliesStatusFilter() {
    ticketService.create(new CreateTicketRequest("Open", "d", Priority.LOW, null));
    UUID progressing = ticketService.create(
        new CreateTicketRequest("Busy", "d", Priority.LOW, null)).id();
    ticketService.changeStatus(progressing, TicketStatus.IN_PROGRESS);

    TicketListResponse result = ticketService.list(null, TicketStatus.IN_PROGRESS);

    assertThat(result.items()).extracting(TicketSummaryResponse::id).containsExactly(progressing);
    assertThat(result.items()).extracting(TicketSummaryResponse::status)
        .containsExactly(TicketStatus.IN_PROGRESS);
  }

  @Test
  void listCombinesKeywordAndStatus() {
    ticketService.create(new CreateTicketRequest("VPN docs", "guide", Priority.LOW, null));
    UUID match = ticketService.create(
        new CreateTicketRequest("VPN outage", "remote", Priority.HIGH, null)).id();
    ticketService.changeStatus(match, TicketStatus.IN_PROGRESS);

    TicketListResponse result = ticketService.list("vpn", TicketStatus.IN_PROGRESS);

    assertThat(result.items()).extracting(TicketSummaryResponse::id).containsExactly(match);
  }

  @Test
  void noMatchesReturnsEmptyItemsNotError() {
    ticketService.create(new CreateTicketRequest("Alpha", "one", Priority.LOW, null));

    TicketListResponse result = ticketService.list("does-not-exist", null);

    assertThat(result.items()).isEmpty();
  }
}
