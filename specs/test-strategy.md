# Test Strategy

Behavior source of truth: `specs/001-support-tickets/spec.md`.
Every acceptance criterion MUST have at least one automated test.
Tests verify behavior, not private implementation.

## Layers

| Layer | Purpose |
|-------|---------|
| Domain / service unit | Lifecycle and field rules with no HTTP. |
| API | HTTP status, JSON shape, error envelope (`MockMvc`). |
| Persistence / integration | Create–read–update and comments on H2. |
| UI unit | Angular 14 component/service specs (`HttpClient` mocked). |

Java 21 / Spring Boot: JUnit 5. Integration tests MAY use H2.
Do not use a shared production database.

## State machine (mandatory)

**Accept:** OPEN→IN_PROGRESS; IN_PROGRESS→RESOLVED; RESOLVED→CLOSED;
OPEN→CANCELLED; IN_PROGRESS→CANCELLED.

**Reject (`409`, status unchanged):** CLOSED→OPEN; RESOLVED→OPEN;
CANCELLED→OPEN; skipped steps; any move from CLOSED or CANCELLED.

## Validation and API

- Missing/blank fields; title length 100 vs 101; invalid enums;
  invalid assignee; invalid list `status` → `400`.
- PATCH does not change status; `assignee: null` clears.
- Comment on CLOSED/CANCELLED → `409` `COMMENT_NOT_ALLOWED`.
- Error envelope: `timestamp`, `status`, `error`, `message`, `details`.

## UI

- Create, list, detail, content update, comments, search, filter.
- Only legal status actions; hide add-comment on terminal statuses.
- Meaningful error display; empty list vs no search matches.

## Traceability

| Spec area | Tests |
|-----------|--------|
| US1 create | API create success/fail; UI create |
| US2 list/detail | API list/get; UI list/detail |
| US3 content update | API PATCH; status unchanged; clear assignee |
| US4 lifecycle | All allowed and required illegal transitions |
| US5 comments | API comment; forbidden on CLOSED/CANCELLED |
| US6 search/filter | keyword, status, both, empty results, newest-first |
| Persistence | Accepted rows still readable after new context |
