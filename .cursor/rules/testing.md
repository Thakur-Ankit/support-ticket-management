# Testing Guidelines

## General

Tests must verify behavior rather than implementation details.

Every acceptance criterion must have at least one corresponding test.

## Backend

Use:

- JUnit 5
- Spring Boot Test
- MockMvc or equivalent API testing
- Mockito only where mocking provides value

## State Machine

The following transitions are valid:

OPEN -> IN_PROGRESS
IN_PROGRESS -> RESOLVED
RESOLVED -> CLOSED
OPEN -> CANCELLED
IN_PROGRESS -> CANCELLED

The following must be rejected:

CLOSED -> OPEN
RESOLVED -> OPEN
CANCELLED -> OPEN

Tests must cover both valid and invalid transitions.

## Validation

Test:

- missing required fields
- invalid values
- excessive field lengths
- invalid status transitions

## API

Test:

- successful requests
- validation errors
- not-found errors
- invalid state transitions
- malformed requests

## Frontend

Test:

- ticket creation
- ticket listing
- ticket details
- updating tickets
- comments
- searching
- filtering
- meaningful error display

## Regression

When a bug is fixed:

1. Add or update a test that reproduces the bug.
2. Fix the implementation.
3. Run the relevant test.
4. Run the complete test suite.