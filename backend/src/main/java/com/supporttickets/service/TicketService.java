package com.supporttickets.service;

import com.supporttickets.api.dto.CommentResponse;
import com.supporttickets.api.dto.CreateCommentRequest;
import com.supporttickets.api.dto.CreateTicketRequest;
import com.supporttickets.api.dto.PatchTicketRequest;
import com.supporttickets.api.dto.TicketDetailResponse;
import com.supporttickets.api.dto.TicketListResponse;
import com.supporttickets.api.dto.TicketSummaryResponse;
import com.supporttickets.api.error.CommentNotAllowedException;
import com.supporttickets.api.error.RequestValidationException;
import com.supporttickets.api.error.ResourceNotFoundException;
import com.supporttickets.domain.TicketStatus;
import com.supporttickets.domain.TicketStatusMachine;
import com.supporttickets.persistence.CommentEntity;
import com.supporttickets.persistence.CommentRepository;
import com.supporttickets.persistence.TicketEntity;
import com.supporttickets.persistence.TicketRepository;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TicketService {

  private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
  private static final int TITLE_MAX = 100;

  private final TicketRepository ticketRepository;
  private final CommentRepository commentRepository;

  public TicketService(TicketRepository ticketRepository, CommentRepository commentRepository) {
    this.ticketRepository = ticketRepository;
    this.commentRepository = commentRepository;
  }

  @Transactional
  public TicketDetailResponse create(CreateTicketRequest request) {
    TicketEntity ticket = new TicketEntity();
    ticket.setTitle(requireTitle(request.title()));
    ticket.setDescription(requireDescription(request.description()));
    ticket.setPriority(request.priority());
    ticket.setAssignee(normalizeOptionalAssignee(request.assignee()));
    ticket.setStatus(TicketStatus.OPEN);
    return toDetail(ticketRepository.save(ticket));
  }

  @Transactional(readOnly = true)
  public TicketListResponse list(String keyword, TicketStatus status) {
    String normalizedKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
    List<TicketSummaryResponse> items = ticketRepository.search(normalizedKeyword, status).stream()
        .map(this::toSummary)
        .toList();
    return new TicketListResponse(items);
  }

  @Transactional(readOnly = true)
  public TicketDetailResponse get(UUID id) {
    return toDetail(requireTicket(id));
  }

  @Transactional
  public TicketDetailResponse patch(UUID id, PatchTicketRequest request) {
    TicketEntity ticket = requireTicket(id);
    TicketStatus statusBefore = ticket.getStatus();

    if (request.titlePresent()) {
      ticket.setTitle(requireTitle(request.title()));
    }
    if (request.descriptionPresent()) {
      ticket.setDescription(requireDescription(request.description()));
    }
    if (request.priorityPresent()) {
      if (request.priority() == null) {
        throw new RequestValidationException("priority", "Priority is required when provided");
      }
      ticket.setPriority(request.priority());
    }
    if (request.assigneePresent()) {
      ticket.setAssignee(normalizeOptionalAssignee(request.assignee()));
    }

    TicketEntity saved = ticketRepository.save(ticket);
    if (saved.getStatus() != statusBefore) {
      throw new IllegalStateException("PATCH must not change status");
    }
    return toDetail(saved);
  }

  @Transactional
  public TicketDetailResponse changeStatus(UUID id, TicketStatus targetStatus) {
    TicketEntity ticket = requireTicket(id);
    TicketStatusMachine.assertAllowed(ticket.getStatus(), targetStatus);
    ticket.setStatus(targetStatus);
    return toDetail(ticketRepository.save(ticket));
  }

  @Transactional
  public CommentResponse addComment(UUID ticketId, CreateCommentRequest request) {
    TicketEntity ticket = requireTicket(ticketId);
    assertCommentsAllowed(ticket.getStatus());

    CommentEntity comment = new CommentEntity();
    comment.setTicket(ticket);
    comment.setContent(requireCommentText(request.content(), "content"));
    comment.setAuthor(requireCommentText(request.author(), "author"));
    return toComment(commentRepository.save(comment));
  }

  private void assertCommentsAllowed(TicketStatus status) {
    if (status == TicketStatus.CLOSED || status == TicketStatus.CANCELLED) {
      throw new CommentNotAllowedException(
          "Comments are not allowed when ticket status is " + status);
    }
  }

  private String requireCommentText(String raw, String field) {
    if (raw == null) {
      throw new RequestValidationException(field, capitalize(field) + " is required");
    }
    String value = raw.trim();
    if (value.isEmpty()) {
      throw new RequestValidationException(field, capitalize(field) + " is required");
    }
    return value;
  }

  private static String capitalize(String field) {
    return field.substring(0, 1).toUpperCase() + field.substring(1);
  }

  private TicketEntity requireTicket(UUID id) {
    return ticketRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + id));
  }

  private String requireTitle(String raw) {
    if (raw == null) {
      throw new RequestValidationException("title", "Title is required");
    }
    String title = raw.trim();
    if (title.isEmpty()) {
      throw new RequestValidationException("title", "Title is required");
    }
    if (title.length() > TITLE_MAX) {
      throw new RequestValidationException("title", "Title must be at most 100 characters");
    }
    return title;
  }

  private String requireDescription(String raw) {
    if (raw == null) {
      throw new RequestValidationException("description", "Description is required");
    }
    String description = raw.trim();
    if (description.isEmpty()) {
      throw new RequestValidationException("description", "Description is required");
    }
    return description;
  }

  private String normalizeOptionalAssignee(String raw) {
    if (raw == null) {
      return null;
    }
    String assignee = raw.trim();
    if (assignee.isEmpty()) {
      throw new RequestValidationException("assignee", "Assignee must be a valid email when provided");
    }
    if (!EMAIL.matcher(assignee).matches()) {
      throw new RequestValidationException("assignee", "Assignee must be a valid email");
    }
    return assignee;
  }

  private TicketSummaryResponse toSummary(TicketEntity ticket) {
    return new TicketSummaryResponse(
        ticket.getId(),
        ticket.getTitle(),
        ticket.getStatus(),
        ticket.getPriority(),
        ticket.getAssignee(),
        ticket.getCreatedAt());
  }

  private TicketDetailResponse toDetail(TicketEntity ticket) {
    List<CommentResponse> comments = commentRepository
        .findByTicketIdOrderByCreatedAtAsc(ticket.getId())
        .stream()
        .map(this::toComment)
        .toList();
    return new TicketDetailResponse(
        ticket.getId(),
        ticket.getTitle(),
        ticket.getDescription(),
        ticket.getPriority(),
        ticket.getStatus(),
        ticket.getAssignee(),
        ticket.getCreatedAt(),
        ticket.getUpdatedAt(),
        comments);
  }

  private CommentResponse toComment(CommentEntity comment) {
    return new CommentResponse(
        comment.getId(),
        comment.getContent(),
        comment.getAuthor(),
        comment.getCreatedAt());
  }
}
