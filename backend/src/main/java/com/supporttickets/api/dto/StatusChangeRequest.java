package com.supporttickets.api.dto;

import com.supporttickets.domain.TicketStatus;
import jakarta.validation.constraints.NotNull;

public record StatusChangeRequest(@NotNull TicketStatus status) {
}
