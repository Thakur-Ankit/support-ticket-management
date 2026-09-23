# API Contract

Behavior source of truth: `specs/001-support-tickets/spec.md`.
HTTP conventions: `.cursor/rules/api-standards.md`.
Feature contract copy: `specs/001-support-tickets/contracts/rest-api.md`.
The running API MUST match this document.

Base path: `/api/v1`. JSON UTF-8. Field names camelCase.
Enums: uppercase strings as in the data model.

## Endpoints

| Method | Path | Purpose |
|--------|------|---------|
| `POST` | `/api/v1/tickets` | Create ticket. Status is always OPEN. |
| `GET` | `/api/v1/tickets` | List tickets. Query: `keyword`, `status`. Newest created first. |
| `GET` | `/api/v1/tickets/{id}` | Ticket detail including comments. |
| `PATCH` | `/api/v1/tickets/{id}` | Update title, description, priority, assignee. MUST NOT change status. |
| `POST` | `/api/v1/tickets/{id}/status` | Request a status transition. Server enforces the state machine. |
| `POST` | `/api/v1/tickets/{id}/comments` | Add a comment (not when CLOSED or CANCELLED). |

There is no delete ticket or edit/delete comment in this version.

## Query parameters (list)

| Name | Meaning |
|------|---------|
| `keyword` | Optional. Case-insensitive substring match on title and description. Empty or omitted: no keyword constraint. |
| `status` | Optional. One of the status enum values. Omitted: all statuses. Invalid value: `400` validation, not an empty list. |

When both are present, results MUST match both (AND).

## Success responses

| Operation | Status | Body |
|-----------|--------|------|
| Create | `201` + `Location: /api/v1/tickets/{id}` | Ticket |
| List | `200` | `{ "items": [ TicketSummary, ... ] }` |
| Get | `200` | TicketDetail (fields + comments oldest-first) |
| Patch | `200` | TicketDetail |
| Status | `200` | TicketDetail |
| Comment | `201` | The created Comment |

### Create ticket body

Required: `title`, `description`, `priority`.
Optional: `assignee`.
Clients MUST NOT send `status`; server sets `OPEN`.

### Patch ticket body

Any of: `title`, `description`, `priority`, `assignee`.
Omitted fields stay unchanged. `assignee: null` clears assignee.

### Status body

`{ "status": "<target status>" }`

### Create comment body

Required: `content`, `author`.

## Errors

```json
{
  "timestamp": "2026-09-23T05:35:00Z",
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "details": [
    { "field": "title", "message": "Title is required" }
  ]
}
```

| Situation | HTTP | `error` |
|-----------|------|---------|
| Missing/invalid fields, bad enum, invalid filter status, whitespace-only | `400` | `VALIDATION_ERROR` |
| Unknown ticket id | `404` | `NOT_FOUND` |
| Illegal status transition | `409` | `ILLEGAL_TRANSITION` |
| Comment on CLOSED or CANCELLED | `409` | `COMMENT_NOT_ALLOWED` |

Do not return `200` with an error payload. Stored data MUST NOT change
on `400`, `404`, or `409`.
