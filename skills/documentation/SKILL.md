# Documentation Skill

## Purpose

Maintain accurate, synchronized project documentation throughout development.

## Source of Truth

The specification defines intended behavior.

The implementation must satisfy the specification.

The documentation must describe the actual agreed behavior.

## Required Documentation

Maintain:

- requirements
- architecture
- data model
- API contract
- state machine
- UI flow
- test strategy
- setup instructions
- development workflow
- prompt history
- AI mistake log

## Documentation Workflow

When requirements change:

1. Update requirements/specification.
2. Review architecture impact.
3. Review data-model impact.
4. Review API-contract impact.
5. Review state-machine impact.
6. Review UI-flow impact.
7. Review testing impact.
8. Update implementation.
9. Update tests.
10. Review documentation for consistency.

## AI Documentation Rules

AI must not invent undocumented requirements.

AI must distinguish:

- confirmed requirements
- assumptions
- implementation decisions
- unresolved questions

## AI Mistakes

Whenever AI produces an incorrect recommendation:

- record the original recommendation
- explain why it was incorrect
- document the corrected decision
- identify the affected artifact/code
- add a regression test when appropriate

## Completion

Documentation is complete only when:

- requirements are traceable to implementation
- implementation is covered by tests
- APIs match the documented contract
- state-machine documentation matches backend behavior
- README setup instructions work
- prompt history is present
- AI mistakes are documented