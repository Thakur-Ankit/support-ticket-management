package com.supporttickets.api.dto;

import com.supporttickets.domain.Priority;
import com.supporttickets.domain.TicketStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TicketDetailResponse(
    UUID id,
    String title,
    String description,
    Priority priority,
    TicketStatus status,
    String assignee,
    Instant createdAt,
    Instant updatedAt,
    List<CommentResponse> comments
) {
}
