package com.supporttickets.api.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.supporttickets.domain.Priority;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

public class PatchTicketRequestDeserializer extends StdDeserializer<PatchTicketRequest> {

  private static final Set<String> ALLOWED_FIELDS = Set.of("title", "description", "priority", "assignee");

  public PatchTicketRequestDeserializer() {
    super(PatchTicketRequest.class);
  }

  @Override
  public PatchTicketRequest deserialize(JsonParser parser, DeserializationContext context)
      throws IOException {
    JsonNode node = parser.getCodec().readTree(parser);
    if (node == null || !node.isObject()) {
      throw JsonMappingException.from(parser, "Patch body must be a JSON object");
    }

    Iterator<String> fieldNames = node.fieldNames();
    while (fieldNames.hasNext()) {
      String name = fieldNames.next();
      if (!ALLOWED_FIELDS.contains(name)) {
        throw JsonMappingException.from(parser, "Unknown property: " + name);
      }
    }

    boolean titlePresent = node.has("title");
    String title = titlePresent && !node.get("title").isNull() ? node.get("title").asText() : null;

    boolean descriptionPresent = node.has("description");
    String description = descriptionPresent && !node.get("description").isNull()
        ? node.get("description").asText()
        : null;

    boolean priorityPresent = node.has("priority");
    Priority priority = null;
    if (priorityPresent && !node.get("priority").isNull()) {
      String raw = node.get("priority").asText();
      try {
        priority = Priority.valueOf(raw);
      } catch (IllegalArgumentException ex) {
        throw InvalidFormatException.from(parser, "Invalid priority: " + raw, raw, Priority.class);
      }
    }

    boolean assigneePresent = node.has("assignee");
    String assignee = null;
    if (assigneePresent && !node.get("assignee").isNull()) {
      assignee = node.get("assignee").asText();
    }

    return new PatchTicketRequest(
        title,
        titlePresent,
        description,
        descriptionPresent,
        priority,
        priorityPresent,
        assignee,
        assigneePresent);
  }
}
