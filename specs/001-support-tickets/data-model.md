# Data Model: Support Ticket Management

Aligned with project doc [../data-model.md](../data-model.md) and [spec.md](./spec.md).

## Ticket

| Field | Persistence | Rules |
|-------|-------------|--------|
| id | UUID PK | Generated on insert |
| title | VARCHAR(100) | Required; trim; length 1–100 |
| description | TEXT | Required; not whitespace-only |
| priority | VARCHAR enum | LOW, MEDIUM, HIGH, URGENT |
| status | VARCHAR enum | OPEN at create; only allowed transitions thereafter |
| assignee | VARCHAR nullable | Null = unassigned; email if set; JSON null on PATCH clears |
| createdAt | TIMESTAMPTZ | Set on insert |
| updatedAt | TIMESTAMPTZ | Set on successful content or status write |

## Comment

| Field | Persistence | Rules |
|-------|-------------|--------|
| id | UUID PK | Generated on insert |
| ticketId | UUID FK | Required |
| content | TEXT | Required |
| author | VARCHAR | Required |
| createdAt | TIMESTAMPTZ | Set on insert |

Add-only. Insert only if ticket status is OPEN, IN_PROGRESS, or RESOLVED.

## Allowed status transitions

OPEN→IN_PROGRESS; IN_PROGRESS→RESOLVED; RESOLVED→CLOSED;
OPEN→CANCELLED; IN_PROGRESS→CANCELLED. All others rejected.
