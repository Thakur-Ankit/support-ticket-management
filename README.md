# Support Ticket Management

Spec-driven support ticket system: Java 21 / Spring Boot API + Angular 14 UI.

## Prerequisites

- JDK **21**
- **Maven** 3.8+
- **Node.js** 16+ (Angular 14) and npm
- **Angular CLI 14** (`npx @angular/cli@14` is fine)
- **PostgreSQL 16** for primary local runs, **or** H2 via profiles configured in Foundational tasks

Do **not** commit secrets. Copy `backend/src/main/resources/application-local.yml.example` (added in Phase 2) to an untracked `application-local.yml` when needed.

## Repository layout

```text
backend/     Spring Boot 3.4.x (Maven)
frontend/    Angular 14
specs/       Spec Kit feature + design docs
docs/        Requirements, plan acceptance, prompt history
.cursor/     Rules and Spec Kit skills
```

## Backend

```bash
cd backend
mvn test
mvn spring-boot:run
```

API default port: **8080**. Datasource and Flyway are configured in Phase 2 (Foundational).

## Frontend

```bash
cd frontend
npm install
npm test -- --watch=false --browsers=ChromeHeadless
npm start
```

UI default: **http://localhost:4200**. API base URL: `environment.apiBaseUrl` → `http://localhost:8080`.

## Spec workflow

Accepted plan: `docs/plan-acceptance.md`. Tasks: `specs/001-support-tickets/tasks.md`.  
Implement in small phases (Setup → Foundational → user stories). Do not generate the whole app in one step.
