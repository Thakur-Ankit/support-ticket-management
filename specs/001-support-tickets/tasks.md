# Tasks: Support Ticket Management

**Input**: Design documents from `/specs/001-support-tickets/`

**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: Included — constitution Principle IX and spec NFR-006 / Acceptance Criterion 8 require automated coverage of business rules and the state machine. Write failing tests before implementation (TDD).

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Web app**: `backend/src/`, `frontend/src/` (from plan.md)

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure

- [X] T001 Create monorepo layout directories `backend/` and `frontend/` per `specs/001-support-tickets/plan.md`
- [X] T002 Initialize Maven Spring Boot 3.4.x Java 21 project in `backend/pom.xml` with Web, Data JPA, Validation, Flyway, PostgreSQL, H2, and test starters
- [X] T003 [P] Create `backend/src/main/java/com/supporttickets/SupportTicketApplication.java` entry point
- [X] T004 [P] Initialize Angular 14 TypeScript app in `frontend/` (`angular.json`, `package.json`, `src/app/app.module.ts`, `src/app/app-routing.module.ts`)
- [X] T005 [P] Add `frontend/src/environments/environment.ts` and `environment.prod.ts` with `apiBaseUrl` default `http://localhost:8080`
- [X] T006 [P] Document local run notes in `README.md` (JDK 21, Maven, Angular CLI 14, Postgres/H2; no secrets)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [X] T007 Add Flyway migration `backend/src/main/resources/db/migration/V1__tickets_and_comments.sql` creating `ticket` and `comment` tables (UUID PKs; title VARCHAR(100) NOT NULL; description TEXT NOT NULL; priority/status VARCHAR NOT NULL; assignee VARCHAR NULL; created_at/updated_at TIMESTAMPTZ; comment FK to ticket; index on `created_at DESC`)
- [X] T008 [P] Configure `backend/src/main/resources/application.yml` (port 8080, JPA validate, Flyway, Jackson) and `application-test.yml` (H2 MODE=PostgreSQL)
- [X] T009 [P] Add `backend/src/main/resources/application-local.yml.example` with datasource placeholders (no real secrets)
- [X] T010 [P] Implement enums `TicketStatus` and `Priority` in `backend/src/main/java/com/supporttickets/domain/` (OPEN, IN_PROGRESS, RESOLVED, CLOSED, CANCELLED; LOW, MEDIUM, HIGH, URGENT)
- [X] T011 Implement `TicketStatusMachine` in `backend/src/main/java/com/supporttickets/domain/TicketStatusMachine.java` with allowed edges only: OPEN→IN_PROGRESS, IN_PROGRESS→RESOLVED, RESOLVED→CLOSED, OPEN→CANCELLED, IN_PROGRESS→CANCELLED
- [X] T012 [P] Create JPA `TicketEntity` in `backend/src/main/java/com/supporttickets/persistence/TicketEntity.java` (fields per data-model; title length 1–100 after trim; status OPEN on create)
- [X] T013 [P] Create JPA `CommentEntity` in `backend/src/main/java/com/supporttickets/persistence/CommentEntity.java` (content/author required, not whitespace-only)
- [X] T014 [P] Create `TicketRepository` and `CommentRepository` in `backend/src/main/java/com/supporttickets/persistence/`
- [X] T015 Implement error DTO + `GlobalExceptionHandler` in `backend/src/main/java/com/supporttickets/api/error/` mapping to envelope `timestamp`, `status`, `error`, `message`, `details` for VALIDATION_ERROR (400), NOT_FOUND (404), ILLEGAL_TRANSITION (409), COMMENT_NOT_ALLOWED (409)
- [X] T016 [P] Configure CORS for `http://localhost:4200` in `backend/src/main/java/com/supporttickets/api/` (or config class)
- [X] T017 Create empty `TicketController` skeleton at `/api/v1/tickets` in `backend/src/main/java/com/supporttickets/api/TicketController.java` and stub `TicketService` in `backend/src/main/java/com/supporttickets/service/TicketService.java`
- [X] T018 [P] Create Angular `TicketService` HTTP client stub in `frontend/src/app/services/ticket.service.ts` and shared models in `frontend/src/app/models/`
- [X] T019 [P] Create scaffold `TicketStatusMachineTest` in `backend/src/test/java/com/supporttickets/domain/TicketStatusMachineTest.java` with failing stubs for one allowed edge and one illegal edge (file owned here; US4 extends this file — do not recreate)

**Checkpoint**: Foundation ready - user story implementation can now begin

---

## Phase 3: User Story 1 - Record a new support ticket (Priority: P1) 🎯 MVP

**Goal**: Operator can create a ticket with title, description, priority (optional assignee); status OPEN; data survives restart.

**Independent Test**: POST valid ticket → 201 + Location + status OPEN; invalid body → 400; row still present after new Spring context / restart.

### Tests for User Story 1

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [X] T020 [P] [US1] MockMvc test create success and validation failures in `backend/src/test/java/com/supporttickets/api/TicketControllerCreateTest.java` (title 1–100; description required; priority enum; assignee optional email)
- [X] T021 [P] [US1] Integration test create persists with H2 in `backend/src/test/java/com/supporttickets/integration/TicketCreateIntegrationTest.java`

### Implementation for User Story 1

- [X] T022 [P] [US1] Add DTOs `CreateTicketRequest` and ticket response records in `backend/src/main/java/com/supporttickets/api/dto/` (`@NotBlank`/`@Size(max=100)` title; `@Email` optional assignee; do not accept client status)
- [X] T023 [US1] Implement `TicketService.create` in `backend/src/main/java/com/supporttickets/service/TicketService.java` setting status OPEN and timestamps
- [X] T024 [US1] Implement `POST /api/v1/tickets` in `backend/src/main/java/com/supporttickets/api/TicketController.java` returning 201 + `Location`
- [X] T025 [US1] Build Angular create page in `frontend/src/app/pages/ticket-create/` wired to `TicketService.create` with field-level error display
- [X] T026 [US1] Add route for create in `frontend/src/app/app-routing.module.ts` and navigation entry

**Checkpoint**: User Story 1 fully functional and testable independently

---

## Phase 4: User Story 2 - Browse and inspect tickets (Priority: P1)

**Goal**: List tickets (newest created first) and open detail with fields and comments (empty list OK).

**Independent Test**: With ≥1 ticket, GET list shows summary fields newest-first; GET by id returns detail; unknown id → 404.

### Tests for User Story 2

- [X] T027 [P] [US2] MockMvc tests for list order and get/404 in `backend/src/test/java/com/supporttickets/api/TicketControllerReadTest.java`
- [X] T028 [P] [US2] Angular unit tests for list/detail components in `frontend/src/app/pages/ticket-list/ticket-list.component.spec.ts` and `ticket-detail.component.spec.ts` (HttpClient mocked)

### Implementation for User Story 2

- [X] T029 [US2] Implement `TicketRepository` query `order by createdAt desc` and `TicketService.list` / `get` in `backend/src/main/java/com/supporttickets/service/TicketService.java`
- [X] T030 [US2] Implement `GET /api/v1/tickets` and `GET /api/v1/tickets/{id}` in `backend/src/main/java/com/supporttickets/api/TicketController.java` returning `{ "items": [...] }` and TicketDetail (comments oldest-first when present)
- [X] T031 [P] [US2] Build list page in `frontend/src/app/pages/ticket-list/` showing id, title, status, priority, assignee
- [X] T032 [P] [US2] Build detail page in `frontend/src/app/pages/ticket-detail/` showing all fields, timestamps, comments
- [X] T033 [US2] Wire list/detail routes and not-found error display in `frontend/src/app/`

**Checkpoint**: User Stories 1 and 2 work independently

---

## Phase 5: User Story 4 - Lifecycle / state machine (Priority: P1)

**Goal**: Backend enforces allowed transitions; illegal transitions return 409 and leave status unchanged; UI offers only legal next statuses.

**Independent Test**: Drive OPEN→IN_PROGRESS→RESOLVED→CLOSED; cancel from OPEN and IN_PROGRESS; CLOSED/RESOLVED/CANCELLED→OPEN → 409 unchanged.

### Tests for User Story 4

- [X] T034 [US4] Extend existing `backend/src/test/java/com/supporttickets/domain/TicketStatusMachineTest.java` from T019 — add all allowed transitions and required illegal examples (CLOSED/RESOLVED/CANCELLED→OPEN, skipped steps); do not create a second test class (NFR-006; depends on T019)
- [X] T035 [P] [US4] MockMvc status transition tests in `backend/src/test/java/com/supporttickets/api/TicketControllerStatusTest.java` (409 ILLEGAL_TRANSITION; status unchanged)
- [X] T036 [P] [US4] Integration happy-path status flow in `backend/src/test/java/com/supporttickets/integration/TicketLifecycleIntegrationTest.java`

### Implementation for User Story 4

- [X] T037 [P] [US4] Add `StatusChangeRequest` DTO in `backend/src/main/java/com/supporttickets/api/dto/StatusChangeRequest.java`
- [X] T038 [US4] Implement `TicketService.changeStatus` calling `TicketStatusMachine.assertAllowed` then persist + bump `updatedAt` in `backend/src/main/java/com/supporttickets/service/TicketService.java`
- [X] T039 [US4] Implement `POST /api/v1/tickets/{id}/status` in `backend/src/main/java/com/supporttickets/api/TicketController.java`
- [X] T040 [US4] Add status action buttons on detail page in `frontend/src/app/pages/ticket-detail/` showing only legal targets; show server message on 409 without painting rejected status

**Checkpoint**: Lifecycle enforced server-side; UI cannot bypass

---

## Phase 6: User Story 3 - Update ticket content (Priority: P2)

**Goal**: PATCH title, description, priority, assignee without changing status; allowed on CLOSED/CANCELLED; `assignee: null` clears; last-write-wins.

**Independent Test**: PATCH on OPEN and CLOSED updates fields, status unchanged; clear assignee; invalid PATCH → 400.

### Tests for User Story 3

- [X] T041 [P] [US3] MockMvc PATCH tests (including terminal status and null assignee) in `backend/src/test/java/com/supporttickets/api/TicketControllerPatchTest.java`
- [X] T042 [P] [US3] Angular edit-page unit test in `frontend/src/app/pages/ticket-edit/ticket-edit.component.spec.ts`

### Implementation for User Story 3

- [X] T043 [P] [US3] Add `PatchTicketRequest` DTO (optional fields; null assignee clears) in `backend/src/main/java/com/supporttickets/api/dto/PatchTicketRequest.java`
- [X] T044 [US3] Implement `TicketService.patch` (must not change status) in `backend/src/main/java/com/supporttickets/service/TicketService.java`
- [X] T045 [US3] Implement `PATCH /api/v1/tickets/{id}` in `backend/src/main/java/com/supporttickets/api/TicketController.java`
- [X] T046 [US3] Build edit page in `frontend/src/app/pages/ticket-edit/` available for every status including CLOSED/CANCELLED

**Checkpoint**: Content updates work without lifecycle side effects

---

## Phase 7: User Story 5 - Comments (Priority: P2)

**Goal**: Add comments on OPEN/IN_PROGRESS/RESOLVED; reject on CLOSED/CANCELLED with COMMENT_NOT_ALLOWED; list oldest-first.

**Independent Test**: Comment on OPEN → 201; on CLOSED → 409; empty content → 400; unknown ticket → 404.

### Tests for User Story 5

- [X] T047 [P] [US5] MockMvc comment tests in `backend/src/test/java/com/supporttickets/api/TicketControllerCommentTest.java`
- [X] T048 [P] [US5] Angular detail comment-form unit test hiding add-comment on terminal statuses in `frontend/src/app/pages/ticket-detail/ticket-detail.component.spec.ts`

### Implementation for User Story 5

- [X] T049 [P] [US5] Add `CreateCommentRequest` DTO in `backend/src/main/java/com/supporttickets/api/dto/CreateCommentRequest.java` (content/author required, not whitespace-only)
- [X] T050 [US5] Implement `TicketService.addComment` rejecting CLOSED/CANCELLED before insert in `backend/src/main/java/com/supporttickets/service/TicketService.java`
- [X] T051 [US5] Implement `POST /api/v1/tickets/{id}/comments` in `backend/src/main/java/com/supporttickets/api/TicketController.java` returning 201
- [X] T052 [US5] Add comment form on detail page in `frontend/src/app/pages/ticket-detail/` only when status is OPEN, IN_PROGRESS, or RESOLVED; show COMMENT_NOT_ALLOWED errors

**Checkpoint**: Comments rules match clarifications

---

## Phase 8: User Story 6 - Search and filter (Priority: P2)

**Goal**: List supports `keyword` (case-insensitive substring on title/description) and `status` filter with AND; invalid status → 400; empty results ≠ error; newest-first preserved.

**Independent Test**: Seed mixed tickets; filter by keyword, by status, both; bad status query → 400; no matches → empty `items`.

### Tests for User Story 6

- [X] T053 [P] [US6] MockMvc list query tests in `backend/src/test/java/com/supporttickets/api/TicketControllerSearchTest.java`
- [X] T054 [P] [US6] Angular list search/filter unit test in `frontend/src/app/pages/ticket-list/ticket-list.component.spec.ts`

### Implementation for User Story 6

- [X] T055 [US6] Extend `TicketRepository` / `TicketService.list(keyword, status)` with AND semantics and invalid enum → validation in `backend/src/main/java/com/supporttickets/`
- [X] T056 [US6] Wire query params on `GET /api/v1/tickets` in `backend/src/main/java/com/supporttickets/api/TicketController.java`
- [X] T057 [US6] Add keyword input and status filter controls on list page in `frontend/src/app/pages/ticket-list/` with empty-state “no matches” distinct from errors

**Checkpoint**: All six user stories independently functional

---

## Phase 9: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

- [ ] T058 [P] Sync project docs `specs/api-contract.md` and `specs/001-support-tickets/contracts/rest-api.md` with implemented behavior
- [ ] T059 [P] Update `docs/prompt-history.md` with this tasks generation entry
- [ ] T060 Run full `mvn test` in `backend/` and `ng test --watch=false` in `frontend/`; fix failures
- [ ] T061 Execute scenarios in `specs/001-support-tickets/quickstart.md` against local stack
- [ ] T062 [P] Confirm no secrets in Git; root `.gitignore` covers `application-local.yml`, `.env*`, H2 `*.mv.db`, Maven `target/`, Angular `node_modules/`/`dist/`/`.angular/`
- [ ] T063 Human review gate: verify constitution checklist (layers, state machine tests, Angular 14, error envelope) before merge
- [ ] T064 [P] NFR-003 check: seed ~1,000 tickets in H2 via `backend/src/test/java/com/supporttickets/integration/TicketListPerformanceIT.java` (or equivalent); assert `GET /api/v1/tickets` and keyword/status filtered list complete in under 3 seconds wall time on a typical local machine; document result in test name or short comment in `specs/001-support-tickets/quickstart.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — can start immediately
- **Foundational (Phase 2)**: Depends on Setup — BLOCKS all user stories
- **User Stories (Phases 3–8)**: All depend on Foundational
  - Prefer priority order: US1 → US2 → US4 → US3 → US5 → US6
  - After Foundational, US3/US5/US6 can proceed in parallel if staffed (still need US1 data for meaningful UI demos)
- **Polish (Phase 9)**: Depends on desired stories being complete

### User Story Dependencies

- **US1 (P1)**: After Foundational — no story dependencies — **MVP**
- **US2 (P1)**: After Foundational — needs created tickets (US1) for useful demo; API independently testable with fixtures
- **US4 (P1)**: After Foundational — needs tickets (US1); detail UI benefits from US2
- **US3 (P2)**: After Foundational — needs tickets; UI benefits from US2
- **US5 (P2)**: After Foundational — needs tickets + detail (US2 recommended)
- **US6 (P2)**: After Foundational — extends list (US2)

### Within Each User Story

- Tests MUST be written and FAIL before implementation
- DTOs/persistence before services before controllers before UI
- Story complete before moving to next priority when sequential

### Parallel Opportunities

- T003–T006 setup in parallel after T001–T002
- T008–T010, T012–T014, T016, T018–T019 foundational parallels
- Per-story `[P]` test pairs; UI pages marked `[P]` when different files
- T034 extends T019’s `TicketStatusMachineTest` (same file — not parallel with recreating that class)

---

## Parallel Example: User Story 1

```bash
# Tests first (fail):
Task: "MockMvc create tests in backend/.../TicketControllerCreateTest.java"
Task: "Integration create test in backend/.../TicketCreateIntegrationTest.java"

# Then implementation:
Task: "DTOs in backend/.../api/dto/"
Task: "TicketService.create + POST /api/v1/tickets"
Task: "Angular create page in frontend/src/app/pages/ticket-create/"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational
3. Complete Phase 3: User Story 1
4. **STOP and VALIDATE**: create API + create UI independently
5. Demo create → confirm OPEN + persistence

### Recommended early slice

US1 + US2 (create and browse) before lifecycle/comments/search.

### Incremental Delivery

1. Setup + Foundational → foundation ready
2. US1 → MVP create
3. US2 → browse/detail
4. US4 → state machine (critical business rules)
5. US3 → content updates
6. US5 → comments
7. US6 → search/filter
8. Polish → quickstart validation

### Parallel Team Strategy

1. Team completes Setup + Foundational together
2. Then: Dev A US1/US2, Dev B US4, Dev C US3/US5/US6 (coordinate on `TicketService`/`TicketController`)

---

## Notes

- [P] tasks = different files, no incomplete dependencies
- [Story] label maps task to US1–US6 for traceability
- Quote constraints in implementation: title max 100; enums exact; comments banned on CLOSED/CANCELLED; list newest-created-first
- **Plan acceptance recorded**: `docs/plan-acceptance.md` (2026-09-23) — constitution Principle II gate cleared for task-sized implementation
- Commit after each task or logical group
- Avoid generating the entire app in one step (constitution XVII)
