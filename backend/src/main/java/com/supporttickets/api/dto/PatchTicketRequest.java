package com.supporttickets.api.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.supporttickets.domain.Priority;

/**
 * Partial update. Omitted fields stay unchanged. Explicit JSON {@code null} for
 * {@code assignee} clears assignee.
 */
@JsonDeserialize(using = PatchTicketRequestDeserializer.class)
public final class PatchTicketRequest {

  private final String title;
  private final boolean titlePresent;
  private final String description;
  private final boolean descriptionPresent;
  private final Priority priority;
  private final boolean priorityPresent;
  private final String assignee;
  private final boolean assigneePresent;

  public PatchTicketRequest(
      String title,
      boolean titlePresent,
      String description,
      boolean descriptionPresent,
      Priority priority,
      boolean priorityPresent,
      String assignee,
      boolean assigneePresent) {
    this.title = title;
    this.titlePresent = titlePresent;
    this.description = description;
    this.descriptionPresent = descriptionPresent;
    this.priority = priority;
    this.priorityPresent = priorityPresent;
    this.assignee = assignee;
    this.assigneePresent = assigneePresent;
  }

  public String title() {
    return title;
  }

  public boolean titlePresent() {
    return titlePresent;
  }

  public String description() {
    return description;
  }

  public boolean descriptionPresent() {
    return descriptionPresent;
  }

  public Priority priority() {
    return priority;
  }

  public boolean priorityPresent() {
    return priorityPresent;
  }

  public String assignee() {
    return assignee;
  }

  public boolean assigneePresent() {
    return assigneePresent;
  }
}
