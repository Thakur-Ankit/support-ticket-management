# AI Mistakes and Corrections

This document records meaningful incorrect AI suggestions discovered during development.

| ID | AI Suggestion | Why It Was Incorrect | Human Correction | Evidence/Test |
|----|---------------|----------------------|------------------|----------------|
| AIM-001 | Plan the UI as React 18 + Vite (or Next.js) and treat Angular 14 as a constitution exception | The mandated client is Angular 14; the operator rejected React | Use Angular 14 (`HttpClient`, NgModules, Karma/Jasmine) | `specs/001-support-tickets/plan.md`, `research.md`; constitution Technology Stack |
