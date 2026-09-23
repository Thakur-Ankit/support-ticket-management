# Support Ticket Management Constitution

## Core Principles

### I. Specification-First Development
Work MUST start from a written requirement and a reviewed specification
before code exists. Specs under `specs/` MUST describe behavior, data,
API, and ticket states. Implementation MUST NOT invent undocumented
endpoints, fields, or transitions.

### II. No Implementation Before Review
Production code, schema migrations, and generated application files
MUST NOT be written until the requirements and the technical plan have
been reviewed and accepted.

### III. Java 21 and Spring Boot Backend
The backend MUST be a Java 21 Spring Boot application (Maven). REST
controllers MUST only bind HTTP, validate input, and map DTOs. Ticket
rules MUST live in services. Persistence MUST go through repositories.
JPA entities MUST NOT be REST bodies.

### IV. PostgreSQL Primary, H2 for Test and Local
PostgreSQL MUST be the primary application database. H2 MAY be used for
automated tests and local development where it does not change
documented behavior. Schema MUST stay in source control.

### V. Consistent REST Semantics
REST APIs MUST use resource-oriented URLs, standard HTTP verbs, JSON
bodies, and the conventions in `.cursor/rules/api-standards.md`.
Breaking changes MUST increment the API version (`/api/v1/` today).

### VI. Authoritative Backend Validation
The server MUST validate every write. Frontend validation MUST NOT
replace backend validation.

### VII. Backend-Enforced Ticket State Machine
Ticket status changes MUST be enforced in backend domain logic as
specified in `specs/state-machine.md`.

### VIII. Reject Invalid State Transitions
Illegal transitions MUST be rejected (`409`, stable `error` code) and
MUST NOT persist the illegal status.

### IX. Automated Tests for Business Rules (NON-NEGOTIABLE)
Business rules MUST have automated tests, especially every legal and
illegal state transition (JUnit 5, MockMvc; H2 for integration).

### X. Synchronized API Contracts
`specs/api-contract.md` (and feature `contracts/`) MUST match the
running API.

### XI. No Secrets in Git
Secrets MUST NOT be committed. Use environment or untracked local config.

### XII. Meaningful Machine-Readable Errors
Error responses MUST use one JSON envelope with `status`, stable
`error`, human-safe `message`, and `details` when validation fails.

### XIII. Documentation Is Part of the Implementation
Specs and operator-facing docs that a change affects MUST be updated
in the same work.

### XIV. Human Review of AI-Generated Code
AI-generated artifacts MUST be reviewed by a human before acceptance.

### XV. Document Incorrect AI Suggestions
Incorrect AI suggestions MUST be recorded in `docs/ai-mistakes.md`.

### XVI. Preserve Meaningful AI Prompts
Meaningful prompts MUST be stored in `docs/prompt-history.md`.

### XVII. Small Reviewable Implementation Steps
Prefer the smallest diff that meets the current task.

### XVIII. Required Delivery Workflow
Requirement → Specification → Plan → Tasks → Implementation →
Testing → Review → Fix.

## Technology Stack

Java 21, Spring Boot, Maven, PostgreSQL (primary), H2 (tests/local),
REST JSON, Angular 14 client.

## Delivery Workflow

Requirements and the technical plan MUST be reviewed before
implementation. Implementation MUST proceed in small steps with tests
for business rules. Review MUST include constitution compliance,
contract sync, secret scan, and human acceptance of AI output.

## Governance

This constitution supersedes informal practice. Amendments MUST bump
version (semver), set Last Amended, and note migrations when needed.

- MAJOR: principle removed/redefined incompatibly, or stack replaced
- MINOR: principle/section added or materially expanded
- PATCH: clarifications and non-semantic fixes

**Version**: 2.0.0 | **Ratified**: 2026-09-23 | **Last Amended**: 2026-09-23
