# Ticket State Machine

Behavior source of truth: `specs/001-support-tickets/spec.md`.
The backend MUST enforce these rules. The UI MUST only offer allowed
next statuses. Persistence MUST NOT store a status the machine rejects.

## Statuses

`OPEN` → `IN_PROGRESS` → `RESOLVED` → `CLOSED`

Also: `OPEN` → `CANCELLED`, `IN_PROGRESS` → `CANCELLED`

`CLOSED` and `CANCELLED` are terminal.

New tickets start in `OPEN`. Status is not chosen by the operator on
create. Content updates MUST NOT change status.

## Allowed transitions

| From | To | Meaning |
|------|----|---------|
| OPEN | IN_PROGRESS | Start work |
| IN_PROGRESS | RESOLVED | Work finished, pending close |
| RESOLVED | CLOSED | Closed |
| OPEN | CANCELLED | Abandoned before work |
| IN_PROGRESS | CANCELLED | Abandoned after work started |

## Rejected transitions

Any pair not in the table above MUST be rejected. Stored status MUST
NOT change. API: HTTP `409`, `error` = `ILLEGAL_TRANSITION`.

Required examples (MUST be tested):

| From | To |
|------|----|
| CLOSED | OPEN |
| RESOLVED | OPEN |
| CANCELLED | OPEN |

Also rejected (non-exhaustive): OPEN → RESOLVED; OPEN → CLOSED;
IN_PROGRESS → OPEN; IN_PROGRESS → CLOSED; RESOLVED → IN_PROGRESS;
RESOLVED → CANCELLED; CLOSED → any; CANCELLED → any; same-status.

## Diagram

```
OPEN ──────► IN_PROGRESS ──────► RESOLVED ──────► CLOSED
  │                │
  └──── CANCELLED ◄┘
```

## Client rules

- Do not send status on create.
- Do not send status on PATCH content.
- POST `/api/v1/tickets/{id}/status` is the only status write.
- UI shows only legal targets from the current status.
