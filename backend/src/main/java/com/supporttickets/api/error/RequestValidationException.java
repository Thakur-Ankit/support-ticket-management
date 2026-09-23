package com.supporttickets.api.error;

import java.util.List;

public class RequestValidationException extends RuntimeException {

  private final List<ErrorDetail> details;

  public RequestValidationException(String message, List<ErrorDetail> details) {
    super(message);
    this.details = details == null ? List.of() : List.copyOf(details);
  }

  public RequestValidationException(String field, String fieldMessage) {
    this("Request validation failed", List.of(new ErrorDetail(field, fieldMessage)));
  }

  public List<ErrorDetail> getDetails() {
    return details;
  }
}
