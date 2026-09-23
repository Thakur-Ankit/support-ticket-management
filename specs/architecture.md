# Architecture

Behavior source of truth: `specs/001-support-tickets/spec.md`.
This document records agreed technical structure. It MUST NOT add
product behavior that is missing from that spec.

## Confirmed from specification

- Operators create, list, view, update, comment on, search, and filter
  tickets.
- The server is the system of record: it validates input and is the
  only enforcer of ticket status.
- Accepted tickets and comments survive application restart.
- The user interface only offers legal status actions and shows
  meaningful errors.

## Implementation decisions (stack)

Mandated by the project constitution:

| Layer | Choice |
|-------|--------|
| Backend | Java 21, Spring Boot, Maven |
| API | REST, JSON, prefix `/api/v1` |
| Primary store | PostgreSQL |
| Tests / local | H2 where it does not change documented behavior |
| Client | Angular 14, talking only to the documented REST API |

`docs/initial-requirements.md` allows “React/Next.js or equivalent.”
Angular 14 is the project decision (constitution + operator correction).

## Logical structure

```
Operator UI (Angular 14)
        |
        | JSON over HTTP
        v
REST API (controllers, DTOs, exception handler)
        |
        v
Application services (validation, ticket lifecycle)
        |
        v
Persistence (repositories, PostgreSQL or H2)
```

- Controllers bind HTTP only. They MUST NOT encode status rules.
- Services own ticket lifecycle and field validation beyond HTTP.
- Repositories persist tickets and comments. They MUST NOT accept a
  status the service would reject.
- JPA entities MUST NOT be API request/response bodies.

## Repository layout

```
.cursor/rules/          # Cursor engineering rules (canonical)
.cursor/skills/         # Spec Kit agent skills
.cursor/commands/       # Cursor commands
skills/documentation/   # Project documentation skill
specs/                  # Project design docs (this file and siblings)
specs/001-support-tickets/  # Spec Kit feature (spec, plan, research, …)
docs/                   # Requirements, prompt history, AI mistakes
.specify/               # Spec Kit tooling and constitution
backend/                # Spring Boot (to be created at implement)
frontend/               # Angular 14 (to be created at implement)
```

## Persistence

- PostgreSQL holds application data in deployed/primary use.
- H2 MAY back automated tests and local runs.
- Schema lives in source control (Flyway).
- Credentials MUST NOT be committed.

## Out of scope (this version)

No authentication, roles, attachments, notifications, extra services,
or additional datastores.
