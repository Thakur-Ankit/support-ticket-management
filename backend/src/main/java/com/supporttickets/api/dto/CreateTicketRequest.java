package com.supporttickets.api.dto;

import com.supporttickets.domain.Priority;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Create body. Title/description length and whitespace rules are enforced after trim in the service.
 * Clients must not send {@code status} (rejected by unknown-property config).
 */
public record CreateTicketRequest(
    @NotBlank String title,
    @NotBlank String description,
    @NotNull Priority priority,
    @Email String assignee
) {
}
