# Feature Specification: Support Ticket Management

**Feature Branch**: `001-support-tickets`

**Created**: 2026-09-23

**Status**: Draft

**Input**: Functional specification from `docs/initial-requirements.md` (what/why; no implementation).

## Clarifications

### Session 2026-09-23

- Q: After CLOSED or CANCELLED, may content (title, description, priority, assignee) still change? → A: Yes. Content stays editable; only status is frozen.
- Q: After CLOSED or CANCELLED, may operators add comments? → A: No.
- Q: Overlapping content saves? → A: Last successful save wins; no conflict message.
- Q: May assignee be cleared? → A: Yes; optional for the ticket’s whole life.
- Q: Default list order? → A: Newest created first.

## Actors

- **Support operator**: Records, finds, updates, comments on, and progresses tickets.
- **System**: System of record; validates input; sole authority for status changes.

Single shared workspace; no sign-in or roles in this version.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Record a new support ticket (Priority: P1)

**Acceptance Scenarios**:

1. **Given** create form, **When** valid title (1–100), description, priority, **Then** ticket stored as OPEN.
2. **Given** missing/invalid fields, **When** submit, **Then** refused; no ticket stored.
3. **Given** created ticket, **When** app restarts, **Then** ticket still present.

### User Story 2 - Browse and inspect tickets (Priority: P1)

1. **Given** tickets exist, **When** open list (no filters), **Then** see identity, title, status, priority, assignee; newest created first.
2. **Given** a ticket, **When** open detail, **Then** see fields, timestamps, comments oldest-first.
3. **Given** unknown id, **When** view, **Then** not-found message.

### User Story 3 - Update ticket content (Priority: P2)

1. Valid content update → fields change; status unchanged; `updatedAt` advances.
2. Invalid update → refused; stored data unchanged.
3. CLOSED or CANCELLED content update → allowed; status unchanged.
4. Clear assignee → ticket unassigned; status unchanged.

### User Story 4 - Lifecycle (Priority: P1)

1. OPEN → IN_PROGRESS → RESOLVED → CLOSED succeeds.
2. OPEN → CANCELLED and IN_PROGRESS → CANCELLED succeed.
3. CLOSED/RESOLVED/CANCELLED → OPEN (and other illegal moves) refused; status unchanged.
4. UI offers only legal next statuses.

### User Story 5 - Comments (Priority: P2)

1. Comment on OPEN, IN_PROGRESS, or RESOLVED succeeds.
2. Empty content refused.
3. Unknown ticket → not found.
4. Comment on CLOSED or CANCELLED refused; no new comment.

### User Story 6 - Search and filter (Priority: P2)

1. Keyword (case-insensitive on title/description) filters list.
2. Status filter shows only that status.
3. Both → AND.
4. No matches → empty list, not an error. Order remains newest created first.

### Edge Cases

- Unknown enums rejected; title 100 OK / 101 rejected; whitespace-only rejected.
- Empty keyword = no keyword constraint.
- Illegal transitions and terminal comments leave stored data unchanged.
- Concurrent content: last successful save wins.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Create with title, description, priority; start OPEN; assignee optional.
- **FR-002**: List with identity, title, status, priority, assignee; newest created first.
- **FR-003**: View full fields, timestamps, comments.
- **FR-004**: Update title, description, priority, assignee in any status including CLOSED/CANCELLED; status unchanged; assignee clearable; last successful save wins.
- **FR-005**: Add comments on OPEN/IN_PROGRESS/RESOLVED only; reject on CLOSED/CANCELLED; no edit/delete.
- **FR-006**: Keyword search on title and description (case-insensitive substring).
- **FR-007**: Filter by one status; AND with keyword; invalid status filter → validation error.
- **FR-008**: Accepted data survives restart.
- **FR-009**: Server validation is authoritative.
- **FR-010**: Server enforces lifecycle; clients cannot persist rejected status.
- **FR-011**: Allowed: OPEN→IN_PROGRESS; IN_PROGRESS→RESOLVED; RESOLVED→CLOSED; OPEN→CANCELLED; IN_PROGRESS→CANCELLED.
- **FR-012**: Reject all other transitions including CLOSED/RESOLVED/CANCELLED→OPEN; status unchanged.
- **FR-013**: Meaningful errors for validation, not-found, illegal transition, comment-not-allowed.
- **FR-014**: UI supports create, list, detail, edit, status actions, comments, search, filter, errors.
- **FR-015**: UI shows only legal status actions.
- **FR-016**: Priority and status enums as specified.

### Non-Functional Requirements

- **NFR-001**: Durability of accepted writes across restart.
- **NFR-002**: Plain-language UI and errors.
- **NFR-003**: Typical actions under 3 seconds with up to 1,000 tickets (verified by tasks.md T064).
- **NFR-004**: Rules hold for every client, not only the UI.
- **NFR-005**: Failure kinds distinguishable for humans and tests.
- **NFR-006**: Automated checks for all allowed transitions and required illegal examples.
- **NFR-007**: No manual recovery after normal stop/start.

### Key Entities

- **Ticket**: identity, title, description, priority, status, optional assignee, created/updated times.
- **Comment**: identity, ticket, content, author, created time.

## Success Criteria *(mandatory)*

- **SC-001**: New operator creates and finds a ticket in under 2 minutes using on-screen labels.
- **SC-002**: With 50 sample tickets, find by keyword and by status in under 30 seconds.
- **SC-003**: 100% of allowed lifecycle paths show new status after save and restart.
- **SC-004**: 100% of required illegal transitions refused; status unchanged after refresh.
- **SC-005**: 100% of accepted tickets/comments visible after restart.
- **SC-006**: Invalid create/update/comment → visible field message; no stored change.

## Assumptions

- No sign-in/roles; assignee is email string; search excludes comments/assignee.
- Content editable on terminal tickets; new comments forbidden on terminal tickets.
- No attachments, SLA, notifications, requester portal.
- Technology deferred to plan (Java 21 / Spring / PostgreSQL / Angular 14).

## Acceptance Criteria

1. Create, list, view, update content, comment (when allowed), search, filter.
2. New tickets are OPEN.
3. Allowed transitions succeed and persist.
4. Illegal transitions fail with unchanged status (including CLOSED/RESOLVED/CANCELLED→OPEN).
5. Server validation cannot be bypassed by the UI.
6. Accepted data present after restart.
7. UI shows meaningful errors for validation, not-found, illegal transition, comment-not-allowed.
8. Automated checks cover allowed transitions and required illegal examples.
9. User stories P1–P2 pass their acceptance scenarios.
