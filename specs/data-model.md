# Data Model

Behavior source of truth: `specs/001-support-tickets/spec.md`.
Identity type UUID and timestamp type Instant are implementation
mappings of “unique identity” and system-set times.

## Ticket

| Field | Type | Rules |
|-------|------|--------|
| id | UUID | Primary key. Assigned on create. Required on later operations. |
| title | String | Required. Trimmed length 1–100. Not whitespace-only. |
| description | String (text) | Required. Not whitespace-only. |
| priority | Enum | Required. `LOW`, `MEDIUM`, `HIGH`, `URGENT`. |
| status | Enum | Required. `OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`, `CANCELLED`. New tickets are `OPEN`. After create, only allowed transitions may change this field. |
| assignee | String | Optional for the ticket’s whole life. If present, MUST be a valid email. MAY be cleared (unassigned). |
| createdAt | Instant | Set on create. Not operator-editable. |
| updatedAt | Instant | Set on every successful content or status write. |

A ticket has zero or more comments.

Content updates (title, description, priority, assignee) MUST NOT
change `status`. Content updates remain allowed when status is
`CLOSED` or `CANCELLED`.

## Comment

| Field | Type | Rules |
|-------|------|--------|
| id | UUID | Primary key. Assigned on create. |
| ticketId | UUID | Required. References Ticket.id. Unknown ticket → not found. |
| content | String (text) | Required. Not whitespace-only. |
| author | String | Required. Not whitespace-only. |
| createdAt | Instant | Set on create. Not operator-editable. |

Comments are add-only: no edit, delete, or nesting in this version.
Listed oldest-first on the ticket. Comments MUST NOT change ticket
status. New comments allowed only when status is OPEN, IN_PROGRESS,
or RESOLVED; CLOSED/CANCELLED → refuse.

## Enumerations

**Priority:** `LOW` | `MEDIUM` | `HIGH` | `URGENT`

**Status:** `OPEN` | `IN_PROGRESS` | `RESOLVED` | `CLOSED` | `CANCELLED`

Unknown enumeration values are validation failures, not coercions.

## Persistence expectations

- Successfully accepted tickets and comments MUST remain after restart.
- Rejected writes MUST NOT appear after restart.
- Primary store: PostgreSQL. H2 MAY be used for tests/local only.
