# Support Ticket Management System

## Goal

Build a Support Ticket Management System.

## Functional Requirements

### Ticket Creation

A user can create a ticket.

### Ticket Listing

A user can list tickets.

### Ticket Details

A user can view ticket details.

### Ticket Updates

A user can update:

- title
- description
- priority
- assignee

### Comments

A user can add comments to a ticket.

### Search

A user can search tickets by keyword.

### Filtering

A user can filter tickets by status.

### Persistence

Ticket data must survive application restart.

### Validation

Backend input validation is required.

### Errors

The UI must display meaningful errors.

## State Machine

OPEN → IN_PROGRESS → RESOLVED → CLOSED

OPEN → CANCELLED

IN_PROGRESS → CANCELLED

Invalid transitions must be rejected.

Examples:

CLOSED → OPEN
RESOLVED → OPEN
CANCELLED → OPEN

## Acceptance Criteria

...

## Technology Constraints

Java 21
Spring Boot
PostgreSQL/H2
REST API
Angular 14
Cursor
Spec-Driven Development