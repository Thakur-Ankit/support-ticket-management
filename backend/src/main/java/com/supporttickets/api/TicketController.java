package com.supporttickets.api;

import com.supporttickets.api.dto.CommentResponse;
import com.supporttickets.api.dto.CreateCommentRequest;
import com.supporttickets.api.dto.CreateTicketRequest;
import com.supporttickets.api.dto.PatchTicketRequest;
import com.supporttickets.api.dto.StatusChangeRequest;
import com.supporttickets.api.dto.TicketDetailResponse;
import com.supporttickets.api.dto.TicketListResponse;
import com.supporttickets.domain.TicketStatus;
import com.supporttickets.service.TicketService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tickets")
public class TicketController {

  private final TicketService ticketService;

  public TicketController(TicketService ticketService) {
    this.ticketService = ticketService;
  }

  @PostMapping
  public ResponseEntity<TicketDetailResponse> create(@Valid @RequestBody CreateTicketRequest request) {
    TicketDetailResponse body = ticketService.create(request);
    return ResponseEntity.created(URI.create("/api/v1/tickets/" + body.id())).body(body);
  }

  @GetMapping
  public TicketListResponse list(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) TicketStatus status) {
    return ticketService.list(keyword, status);
  }

  @GetMapping("/{id}")
  public TicketDetailResponse get(@PathVariable UUID id) {
    return ticketService.get(id);
  }

  @PatchMapping("/{id}")
  public TicketDetailResponse patch(
      @PathVariable UUID id,
      @RequestBody PatchTicketRequest request) {
    return ticketService.patch(id, request);
  }

  @PostMapping("/{id}/status")
  public TicketDetailResponse changeStatus(
      @PathVariable UUID id,
      @Valid @RequestBody StatusChangeRequest request) {
    return ticketService.changeStatus(id, request.status());
  }

  @PostMapping("/{id}/comments")
  @ResponseStatus(HttpStatus.CREATED)
  public CommentResponse addComment(
      @PathVariable UUID id,
      @Valid @RequestBody CreateCommentRequest request) {
    return ticketService.addComment(id, request);
  }
}
