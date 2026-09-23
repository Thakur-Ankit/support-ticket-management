# REST API Standards

## HTTP

Use appropriate HTTP methods:

POST    create
GET     retrieve
PUT/PATCH update
DELETE  delete where applicable

## JSON

Requests and responses use JSON.

## URLs

Use resource-oriented URLs under `/api/v1`.

Example:

GET    /api/v1/tickets
POST   /api/v1/tickets
GET    /api/v1/tickets/{id}
PATCH  /api/v1/tickets/{id}
POST   /api/v1/tickets/{id}/status
POST   /api/v1/tickets/{id}/comments

## Status Codes

200 OK
201 Created
204 No Content
400 Bad Request
404 Not Found
409 Conflict
422 Unprocessable Entity where appropriate

## Errors

Use a consistent error format.

Example:

{
  "timestamp": "...",
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "details": [
    {
      "field": "title",
      "message": "Title is required"
    }
  ]
}

## API Documentation

API behavior must be documented in the API contract.

The implementation must not silently diverge from the contract.