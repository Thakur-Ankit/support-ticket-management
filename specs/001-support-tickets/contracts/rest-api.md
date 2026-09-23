# REST API Contract (`/api/v1`)

Canonical project copy: [../../api-contract.md](../../api-contract.md).
Implements [spec.md](../spec.md).

## Endpoints

| Method | Path | Success |
|--------|------|---------|
| `POST` | `/api/v1/tickets` | `201` + `Location` |
| `GET` | `/api/v1/tickets` | `200` `{ "items": [...] }` newest first |
| `GET` | `/api/v1/tickets/{id}` | `200` TicketDetail |
| `PATCH` | `/api/v1/tickets/{id}` | `200` TicketDetail |
| `POST` | `/api/v1/tickets/{id}/status` | `200` TicketDetail |
| `POST` | `/api/v1/tickets/{id}/comments` | `201` Comment |

## Errors

| Case | HTTP | `error` |
|------|------|---------|
| Validation | 400 | VALIDATION_ERROR |
| Unknown id | 404 | NOT_FOUND |
| Illegal transition | 409 | ILLEGAL_TRANSITION |
| Comment on CLOSED/CANCELLED | 409 | COMMENT_NOT_ALLOWED |

Envelope: `timestamp`, `status`, `error`, `message`, `details`.
