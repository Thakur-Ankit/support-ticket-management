# UI Flow

Behavior source of truth: `specs/001-support-tickets/spec.md`.
Client: Angular 14. Talks only to `specs/api-contract.md`.
This version is a single shared operator workspace (no sign-in).

## Views

1. **Ticket list** — identity, title, status, priority, assignee;
   keyword search; status filter; newest created first; empty list vs
   “no matches”; navigate to create and to detail.
2. **Create ticket** — title, description, priority, optional
   assignee; on success go to detail or list; on validation error stay
   and show field messages.
3. **Ticket detail** — all fields, timestamps, comments oldest-first,
   add-comment when status is OPEN / IN_PROGRESS / RESOLVED, content
   edit, legal status actions only.
4. **Edit content** — title, description, priority, assignee; does
   not change status; allowed even if CLOSED or CANCELLED.

## List: search and filter

- Keyword: case-insensitive match on title and description.
- Empty keyword: no keyword constraint (status filter may still apply).
- Status filter: one status or all.
- Both applied: AND.
- No matches: empty list and a clear “no matches” message, not an error.

## Status and comments

Show only transitions allowed from the current status
(`specs/state-machine.md`). After `409`, keep previous status on screen.
On CLOSED or CANCELLED, do not present add-comment as a successful path.

## Errors

Show server `message` (and field `details` when present) for
validation (`400`), not found (`404`), illegal transition (`409`),
and comment not allowed (`409`). Client checks MAY run first; the
server remains authoritative.

## Out of scope

Requester portal, login, attachments, editing/deleting comments.
