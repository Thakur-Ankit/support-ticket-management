# Quickstart (validation guide)

Do not implement until this plan is reviewed. After implementation,
prove the feature locally.

## Prerequisites

- JDK 21, Maven 3.9+, Node 16+ (Angular 14), Angular CLI 14
- PostgreSQL 16 **or** H2 via documented local/test profile
- No secrets in Git

## Commands (after code exists)

```bash
cd backend && mvn test
cd ../frontend && ng test --watch=false && ng build
```

API: `cd backend && mvn spring-boot:run` (set `SPRING_DATASOURCE_*` for Postgres).

UI: `cd frontend && ng serve` (`environment.apiBaseUrl=http://localhost:8080`).

## Scenarios

1. Create → `201`, status OPEN; survives restart.
2. List newest-first; keyword + status AND; bad status → `400`.
3. Get detail + comments; unknown id → `404`.
4. PATCH on OPEN and CLOSED; status unchanged; `assignee: null` clears.
5. Status path OPEN→IN_PROGRESS→RESOLVED→CLOSED; cancels from OPEN/IN_PROGRESS.
6. Illegal CLOSED/RESOLVED/CANCELLED→OPEN → `409`, status unchanged.
7. Comment on OPEN OK; on CLOSED/CANCELLED → `409` `COMMENT_NOT_ALLOWED`.
8. UI shows errors; hides add-comment on terminal statuses.
