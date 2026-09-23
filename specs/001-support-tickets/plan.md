# Implementation Plan: Support Ticket Management

**Branch**: `001-support-tickets` | **Date**: 2026-09-23 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/001-support-tickets/spec.md`

## Summary

Operators record, find, update, comment on, and progress support tickets.
The server is the system of record: Jakarta Bean Validation plus a domain
state machine in the service layer. Persistence is PostgreSQL (H2 for tests
and optional local). The UI is an Angular 14 TypeScript SPA that calls only
`/api/v1` as defined in `contracts/`. This plan does not implement the app.

## Technical Context

**Language/Version**: Java 21 (backend); TypeScript (Angular 14 toolchain)

**Primary Dependencies**: Spring Boot 3.4.x (Web, Data JPA, Validation);
Maven; Angular 14; Flyway

**Storage**: PostgreSQL 16 primary; H2 in `test` (and optional `local`) profiles

**Testing**: JUnit 5, Spring Boot Test, MockMvc, AssertJ; `@SpringBootTest` + H2;
Angular 14 Karma/Jasmine

**Target Platform**: Local developer workstation; JVM server + browser

**Project Type**: Web application (REST API + SPA)

**Performance Goals**: Typical operator actions under 3 seconds with up to
1,000 tickets (spec NFR-003)

**Constraints**: No auth; no secrets in Git; last-write-wins content saves;
comments forbidden on CLOSED/CANCELLED; content edits allowed on terminal
tickets; list newest-created-first

**Scale/Scope**: Ticket + Comment; six REST operations; four UI views

## Constitution Check

| Principle | Plan response | Gate |
|-----------|---------------|------|
| Spec-first / no impl before review | Design only | Pass |
| Java 21 / layered Spring | api → service → domain → persistence | Pass |
| PostgreSQL + H2 | Flyway; H2 for tests/local | Pass |
| REST + validation + state machine | `/api/v1`; `409` illegal / comment ban | Pass |
| Tests | JUnit 5 + MockMvc + H2; Angular specs | Pass |
| Angular 14 | NgModules, `HttpClient`, port 4200 CORS | Pass |
| Docs / AI governance | plan, research, contracts, quickstart | Pass |

**Post-Phase 1**: Ready for `/speckit-tasks` after human review of this plan.

**Plan acceptance**: Recorded in `docs/plan-acceptance.md` (2026-09-23). Constitution Principle II gate cleared for implementation in task-sized steps.

## Project Structure

### Documentation (this feature)

```text
specs/001-support-tickets/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/rest-api.md
├── checklists/
└── tasks.md             # NOT created by /speckit-plan
```

Project-wide design docs (architecture, api-contract, state-machine,
ui-flow, test-strategy) live under `specs/` per the documentation skill.

### Source Code (repository root)

```text
backend/
├── pom.xml
└── src/main/java/com/supporttickets/
    ├── api/
    ├── domain/
    ├── service/
    └── persistence/

frontend/
├── angular.json
└── src/app/
    ├── services/ticket.service.ts
    └── pages/ (list, create, detail, edit)
```

**Structure Decision**: Two-root web app (`backend/` Maven, `frontend/`
Angular 14).

## Complexity Tracking

No constitution violations.

## Implementation Design

### Backend architecture

Layered Spring Boot (constructor injection): controllers (HTTP/DTOs),
services (lifecycle, `@Transactional` writes), domain
(`TicketStatusMachine`), persistence (JPA + repositories). Entities are
never API bodies.

### Frontend architecture

Angular 14 NgModules, Router, `HttpClient`. Pages: list, create, detail,
edit. Status buttons mirror allowed edges (display only). No NgRx unless
later justified.

### Database

Flyway SQL; PostgreSQL primary; H2 for tests; UUID PKs; list
`order by created_at desc`.

### Services / controllers / DTOs

`TicketService`: create (OPEN), list (keyword AND status), get, patch
(content; null assignee clears), changeStatus (machine), addComment
(reject CLOSED/CANCELLED). Single `TicketController` under `/api/v1/tickets`.
Request/response Java records with Bean Validation.

### Exception handling

`VALIDATION_ERROR` 400; `NOT_FOUND` 404; `ILLEGAL_TRANSITION` 409;
`COMMENT_NOT_ALLOWED` 409. Envelope per `.cursor/rules/api-standards.md`.

### Testing

Domain unit (all transitions); service unit; `@WebMvcTest`+MockMvc;
`@SpringBootTest`+H2; Angular `*.spec.ts` with mocked HTTP.

### Configuration / local

Port 8080 API; CORS `http://localhost:4200`; env for datasource;
`environment.apiBaseUrl` on Angular. See [quickstart.md](./quickstart.md).
