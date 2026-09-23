package com.supporttickets.api.error;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
    Instant timestamp,
    int status,
    String error,
    String message,
    List<ErrorDetail> details
) {
  public ErrorResponse {
    details = details == null ? List.of() : List.copyOf(details);
  }
}
