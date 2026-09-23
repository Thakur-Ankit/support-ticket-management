# Research: Support Ticket Management

Phase 0 decisions for `001-support-tickets`.

## Frontend framework

**Decision**: Angular 14 TypeScript SPA (`HttpClient`, NgModules, Router).

**Rationale**: Project constitution mandates Angular 14. Operator
corrected an earlier React/Next.js plan. `docs/initial-requirements.md`
“React/Next.js or equivalent” is satisfied by Angular 14.

**Alternatives considered**: React 18 + Vite; Next.js — rejected.

## Error envelope field name

**Decision**: Use `error` as the stable machine string.

**Rationale**: Matches `.cursor/rules/api-standards.md`.

## Comment rejection on terminal tickets

**Decision**: HTTP `409` with `error` = `COMMENT_NOT_ALLOWED`.

## PATCH assignee clear vs omit

**Decision**: Omitted → unchanged. JSON `null` → clear.

## Database migrations

**Decision**: Flyway SQL; JPA `ddl-auto=validate` on Postgres; H2 + Flyway in tests.

## List ordering

**Decision**: SQL `order by created_at desc` (API source of truth).

## State machine placement

**Decision**: Pure Java `TicketStatusMachine` in `domain`, used only from `TicketService`.

## HTTP client on the frontend

**Decision**: Angular `TicketService` wrapping `HttpClient`. No NgRx.
