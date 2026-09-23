package com.supporttickets.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.supporttickets.domain.Priority;
import com.supporttickets.domain.TicketStatus;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class TicketRepositorySearchTest {

  @Autowired
  private TicketRepository ticketRepository;

  @Autowired
  private CommentRepository commentRepository;

  @BeforeEach
  void clean() {
    commentRepository.deleteAll();
    ticketRepository.deleteAll();
  }

  @Test
  void keywordMatchesTitleCaseInsensitive() {
    save("Printer Jam", "hardware", TicketStatus.OPEN, null);
    save("Other", "unrelated", TicketStatus.OPEN, null);

    List<TicketEntity> found = ticketRepository.search("printer", null);

    assertThat(found).extracting(TicketEntity::getTitle).containsExactly("Printer Jam");
  }

  @Test
  void keywordMatchesDescription() {
    save("Ticket A", "core SWITCH failure", TicketStatus.OPEN, null);
    save("Ticket B", "badge reader", TicketStatus.OPEN, null);

    List<TicketEntity> found = ticketRepository.search("switch", null);

    assertThat(found).extracting(TicketEntity::getTitle).containsExactly("Ticket A");
  }

  @Test
  void statusFilterAlone() {
    save("Open one", "d", TicketStatus.OPEN, null);
    TicketEntity inProgress = save("Progress", "d", TicketStatus.IN_PROGRESS, null);

    List<TicketEntity> found = ticketRepository.search(null, TicketStatus.IN_PROGRESS);

    assertThat(found).extracting(TicketEntity::getId).containsExactly(inProgress.getId());
  }

  @Test
  void keywordAndStatusCombinedWithAnd() {
    save("VPN docs", "guide", TicketStatus.OPEN, null);
    TicketEntity match = save("VPN outage", "remote", TicketStatus.IN_PROGRESS, null);
    save("Email outage", "remote", TicketStatus.IN_PROGRESS, null);

    List<TicketEntity> found = ticketRepository.search("VPN", TicketStatus.IN_PROGRESS);

    assertThat(found).extracting(TicketEntity::getId).containsExactly(match.getId());
  }

  @Test
  void nullKeywordMeansNoKeywordConstraint() {
    save("A", "one", TicketStatus.OPEN, null);
    save("B", "two", TicketStatus.CLOSED, null);

    assertThat(ticketRepository.search(null, null)).hasSize(2);
  }

  @Test
  void keywordDoesNotMatchAssignee() {
    save("Title", "description only", TicketStatus.OPEN, "searchme@example.com");

    assertThat(ticketRepository.search("searchme", null)).isEmpty();
  }

  @Test
  void keywordDoesNotMatchCommentContent() {
    TicketEntity ticket = save("Plain title", "plain description", TicketStatus.OPEN, null);
    CommentEntity comment = new CommentEntity();
    comment.setTicket(ticket);
    comment.setContent("unique-comment-token");
    comment.setAuthor("ops@example.com");
    commentRepository.save(comment);

    assertThat(ticketRepository.search("unique-comment-token", null)).isEmpty();
  }

  @Test
  void resultsOrderedNewestCreatedFirst() throws InterruptedException {
    TicketEntity older = save("Older", "d", TicketStatus.OPEN, null);
    Thread.sleep(15);
    TicketEntity newer = save("Newer", "d", TicketStatus.OPEN, null);

    List<TicketEntity> found = ticketRepository.search(null, null);

    assertThat(found).extracting(TicketEntity::getId)
        .containsExactly(newer.getId(), older.getId());
  }

  private TicketEntity save(String title, String description, TicketStatus status, String assignee) {
    TicketEntity ticket = new TicketEntity();
    ticket.setTitle(title);
    ticket.setDescription(description);
    ticket.setPriority(Priority.MEDIUM);
    ticket.setStatus(status);
    ticket.setAssignee(assignee);
    return ticketRepository.saveAndFlush(ticket);
  }
}
