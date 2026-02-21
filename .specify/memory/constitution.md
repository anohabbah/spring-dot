<!--
Sync Impact Report
===================
Version change: 1.1.0 → 1.1.1
Modified principles:
  - I. API-First Design: changed API versioning configuration from
    programmatic (WebMvcConfigurer) to declarative (application.yaml
    properties), aligning with Principle IV (config in YAML)
Added sections: None
Removed sections: None
Templates requiring updates:
  - .specify/templates/plan-template.md ✅ no updates needed
  - .specify/templates/spec-template.md ✅ no updates needed
  - .specify/templates/tasks-template.md ✅ no updates needed
  - .specify/templates/commands/*.md ✅ no command files present
Follow-up TODOs: None
-->

# Spring-Dot Constitution

## Core Principles

### I. API-First Design

- Every feature MUST define its API contract (OpenAPI specification) before
  implementation begins.
- Endpoints MUST be documented via SpringDoc annotations; undocumented
  endpoints are not permitted in production code.
- Request/response DTOs MUST be separate from persistence entities; MapStruct
  MUST be used for mapping between layers.
- API versioning MUST use Spring Boot 4's built-in path segment strategy,
  configured declaratively in `application.yaml` under
  `spring.mvc.apiversion`:
  - `use.path-segment`: MUST be set to `1` (version occupies the first
    path segment after the base path, e.g., `/api/v1/users`).
  - `supported`: MUST list all active versions (e.g., `1.0,2.0`).
  - `default`: MUST specify the fallback version (e.g., `1.0`).
  Programmatic configuration via `WebMvcConfigurer` MUST NOT be used;
  this keeps versioning config co-located with other Spring properties
  per Principle IV.
- All versioned endpoints MUST declare their version via the `version`
  attribute on `@GetMapping`, `@PostMapping`, and other `@RequestMapping`
  annotations (e.g., `@GetMapping(value = "/{version}/users", version = "1.0")`).
- Path segment versioning MUST NOT be mixed with header, query parameter,
  or media-type versioning strategies within this project.

**Rationale**: The project already integrates SpringDoc OpenAPI. Designing
contracts first prevents rework, enables parallel frontend/backend development,
and produces accurate, always-up-to-date documentation. Spring Boot 4's
native path segment versioning eliminates custom versioning hacks and
provides cache-friendly, RESTful URLs that are ideal for public-facing APIs.

### II. Test-Driven Development (NON-NEGOTIABLE)

- The Red-Green-Refactor cycle MUST be followed: write a failing test, make
  it pass with minimal code, then refactor.
- Integration tests MUST use Testcontainers with PostgreSQL; mocking the
  database layer in integration tests is prohibited.
- Every user-facing endpoint MUST have at least one integration test that
  exercises the full request-response path including the database.
- Unit tests MUST cover business logic in service classes; trivial delegation
  methods (getters, setters, MapStruct mappers) do not require unit tests.

**Rationale**: Testcontainers infrastructure is already configured. TDD catches
regressions early, documents expected behavior, and enforces clean interfaces.

### III. Database Migration Discipline

- All schema changes MUST be expressed as Flyway versioned migrations in
  `src/main/resources/db/migration/`.
- Migration files MUST follow the naming convention `V{version}__{description}.sql`
  (e.g., `V1__create_users_table.sql`).
- Migrations MUST be additive and forward-only in shared branches; destructive
  changes (DROP TABLE, DROP COLUMN) require explicit review and approval.
- Every migration MUST be tested by running the full migration chain against
  a fresh Testcontainers PostgreSQL instance.

**Rationale**: Flyway is configured and PostgreSQL is the target database.
Disciplined migrations prevent data loss, enable reproducible environments,
and keep schema history auditable.

### IV. Simplicity & YAGNI

- No abstraction layer MUST be introduced until at least two concrete use
  cases demand it.
- Spring Data JDBC MUST be used for data access; JPA/Hibernate MUST NOT be
  added unless justified by a documented complexity-tracking entry.
- Configuration MUST live in `application.yaml`; property files or environment
  variable overrides are acceptable but MUST NOT duplicate values already in
  YAML.
- New dependencies MUST be justified in the PR description; transitive
  dependency bloat MUST be monitored.

**Rationale**: The project is greenfield. Premature abstraction and dependency
accumulation are the primary sources of accidental complexity in Spring
applications. Start lean, grow intentionally.

### V. Observability

- Every service method that performs a side effect (database write, external
  call) MUST log entry and outcome at INFO level using structured logging
  (key-value pairs or JSON).
- Error paths MUST log at WARN or ERROR level with sufficient context to
  reproduce the issue without a debugger.
- Spring Boot Actuator health and info endpoints MUST remain enabled; custom
  health indicators MUST be added for any external dependency (database,
  third-party API).
- Performance-sensitive paths SHOULD be instrumented with timing metrics.

**Rationale**: Observability is cheaper to build in from the start than to
retrofit. Structured logs and health endpoints enable rapid incident response
and proactive monitoring.

## Technology Standards

- **Language**: Java 21 (LTS). Use modern language features (records, sealed
  classes, pattern matching) where they improve clarity.
- **Framework**: Spring Boot 4.0.3 with Spring Web MVC.
- **Data Access**: Spring Data JDBC. JPA/Hibernate is explicitly excluded
  (see Principle IV).
- **Database**: PostgreSQL. All environments (dev, test, CI, prod) MUST use
  PostgreSQL; H2 or other in-memory databases are prohibited.
- **Schema Management**: Flyway (see Principle III).
- **Object Mapping**: MapStruct 1.7 with Spring component model integration.
- **Boilerplate Reduction**: Lombok via the Freefair Gradle plugin.
- **API Documentation**: SpringDoc OpenAPI 3.0.1 with WebMVC UI.
- **Build Tool**: Gradle with the Spring Boot and Spring Dependency Management
  plugins.
- **Testing**: JUnit 5 + Spring Boot Test + Testcontainers (PostgreSQL).

## Development Workflow

- **Branching**: Feature branches MUST branch from `main` and be merged back
  via pull request. Direct commits to `main` are prohibited.
- **Commit Messages**: Follow Conventional Commits format
  (`type: description`, e.g., `feat: add user registration endpoint`).
- **Pull Requests**: Every PR MUST include a description of what changed and
  why. PRs MUST pass all automated tests before merge.
- **Testing Gate**: CI MUST run the full test suite (unit + integration via
  Testcontainers) on every PR. A failing test blocks merge.
- **Code Review**: At least one approval is required before merge. Reviewers
  MUST verify compliance with this constitution's principles.
- **Database Changes**: PRs containing Flyway migrations MUST be flagged for
  explicit migration review.

## Governance

- This constitution supersedes ad-hoc decisions and informal conventions.
  When a conflict arises, the constitution is authoritative.
- Amendments MUST be documented with a version bump, rationale, and
  migration plan if existing code is affected.
- Version increments follow semantic versioning:
  - **MAJOR**: Principle removal or backward-incompatible redefinition.
  - **MINOR**: New principle, new section, or material expansion of guidance.
  - **PATCH**: Clarifications, wording improvements, typo fixes.
- All PRs and code reviews MUST verify compliance with the active
  constitution version.
- Complexity MUST be justified via the Complexity Tracking table in
  implementation plans (see plan template).
- This constitution SHOULD be reviewed quarterly or whenever a major
  architectural decision is made.

**Version**: 1.1.1 | **Ratified**: 2026-02-21 | **Last Amended**: 2026-02-21
