package com.supporttickets.api.dto;

import com.supporttickets.domain.Priority;
import com.supporttickets.domain.TicketStatus;
import java.time.Instant;
import java.util.UUID;

public record TicketSummaryResponse(
    UUID id,
    String title,
    TicketStatus status,
    Priority priority,
    String assignee,
    Instant createdAt
) {
}
