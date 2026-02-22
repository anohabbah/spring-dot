# Research: CRUD Checklist Items

**Feature**: 001-crud-checklist-items
**Date**: 2026-02-21

## Overview

No NEEDS CLARIFICATION items exist — the tech stack is fully defined by the constitution. Research focused on best practices for the specific technologies mandated by the project.

## Decisions

### 1. Primary Key Strategy

**Decision**: BIGSERIAL (auto-incrementing bigint)
**Rationale**: Simpler than UUID for a single-service application. More storage-efficient (8 bytes vs 16), produces shorter URLs, and has better index performance. Spring Data JDBC's `save()` method uses `@Id` null-check to distinguish INSERT (id is null) from UPDATE (id is non-null), which works naturally with BIGSERIAL.
**Alternatives considered**:
- UUID: Better for distributed systems or client-generated IDs, but adds complexity (requires `Persistable<UUID>` implementation for Spring Data JDBC insert detection). Not needed for this scope.

### 2. Spring Data JDBC Entity Pattern

**Decision**: Java record with `@Table`, `@Id` annotations. Use `withId()` wither method for post-insert ID population.
**Rationale**: Records are immutable and align with the constitution's preference for records. Spring Data JDBC requires a wither method to return a new instance with the database-generated ID after insert.
**Alternatives considered**:
- Mutable class with Lombok `@Data`: More traditional but violates the record-first principle and introduces mutability.

### 3. Repository Interface

**Decision**: Extend `ListCrudRepository<ChecklistItemEntity, Long>` for `List`-returning find methods.
**Rationale**: `ListCrudRepository` provides `findAll()` returning `List<T>` instead of `Iterable<T>`, which is more convenient for stream operations and mapping.
**Alternatives considered**:
- `CrudRepository`: Returns `Iterable<T>` from `findAll()`, requires manual conversion to `List`.

### 4. API Versioning Configuration

**Decision**: Path-segment versioning via `spring.mvc.apiversion` in `application.yaml` with `use.path-segment: 0`, `supported: 1.0`, `default: 1.0`. Controller path: `/{version}/checklist`.
**Rationale**: Spring Boot 4's built-in path-segment strategy, mandated by constitution Principle I. Path segment index `0` means the version is the first segment (e.g., `/v1/checklist`).
**Alternatives considered**:
- Header versioning, query parameter versioning: Explicitly prohibited by constitution.
- Programmatic `WebMvcConfigurer`: Explicitly prohibited by constitution.

### 5. REST Resource Naming

**Decision**: `/api/{version}/checklist-items` (plural, hyphenated)
**Rationale**: RESTful convention uses plural nouns for collection resources. Hyphen-case is the standard for URL path segments.
**Alternatives considered**:
- `/api/{version}/checklistItems` (camelCase): Not standard for URLs.
- `/api/{version}/items`: Too generic, would conflict with other features.

### 6. Update Strategy

**Decision**: Full replacement via PUT with both `name` and `checked` fields required.
**Rationale**: Simpler than PATCH for a two-field entity. PUT semantics are clear: the entire resource representation is replaced. Validation applies uniformly.
**Alternatives considered**:
- PATCH with partial updates: More complex, better suited for entities with many optional fields. Overkill for two fields.

### 7. Error Response Pattern

**Decision**: Use Spring Boot's default `ProblemDetail` (RFC 9457) error responses. Throw domain exceptions mapped to HTTP status codes via `@ResponseStatus` or `@ExceptionHandler`.
**Rationale**: Spring Boot 4 supports RFC 9457 Problem Details natively. No custom error response structure needed.
**Alternatives considered**:
- Custom error response DTOs: Adds unnecessary complexity when the framework provides a standard.

### 8. MockMvcTester Testing Pattern

**Decision**: Use `MockMvcTester` with fluent AssertJ assertions. Use `bodyJson().extractingPath()` for JSON field assertions and `bodyJson().isLenientlyEqualTo()` for full-body comparisons.
**Rationale**: Mandated by constitution Principle II (v3.1.0). MockMvcTester provides a cleaner API than legacy `MockMvc` + `MockMvcResultMatchers`.
**Alternatives considered**:
- Legacy `MockMvc` with `andExpect()`: Older API, not mandated.
- `WebTestClient`: Requires reactive stack, not applicable.
