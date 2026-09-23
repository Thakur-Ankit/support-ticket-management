# Java Spring Boot Guidelines

## Java

- Use Java 21.
- Prefer records for immutable DTOs where appropriate.
- Use meaningful names.
- Avoid unnecessary abstraction.
- Follow standard Java naming conventions.
- Keep methods focused on one responsibility.
- Do not suppress compiler warnings without justification.

## Spring Boot

- Use constructor injection.
- Do not use field injection.
- Separate Controller, Service, Repository and Domain responsibilities.
- Controllers handle HTTP concerns.
- Services contain business rules.
- Repositories handle persistence.
- Domain/state transition rules must not be implemented only in controllers.

## Validation

- Validate request DTOs using Jakarta Bean Validation.
- Never trust frontend validation.
- Validate required fields, lengths and allowed values.
- Return meaningful validation errors.

## Error Handling

- Use centralized exception handling.
- Return consistent JSON error responses.
- Do not expose stack traces to API consumers.
- Do not expose internal implementation details.

## Database

- Use PostgreSQL for the primary application database.
- H2 may be used for tests where appropriate.
- Use migrations/schema management where appropriate.
- Avoid hard-coded database credentials.

## Security

- Never commit secrets.
- Configuration must use environment variables or local configuration.
- Never place passwords, API keys or tokens in source code.

## Testing

- Business rules must have automated tests.
- State transitions must have explicit tests.
- Controllers should have API-level tests.
- Repository/database behavior should be tested where appropriate.