<!--
Sync Impact Report
===================
Version change: 2.0.2 → 3.0.0
Modified principles:
  - II. Test-Driven Development: removed unit test mandate; only integration
    tests (via Testcontainers) are now permitted
Modified sections:
  - Technology Standards → Testing: removed domain layer unit test rule
  - Development Workflow → Testing Gate: removed "unit +" reference
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
- Only integration tests are permitted. Unit tests MUST NOT be written.
- All integration tests MUST use Testcontainers with PostgreSQL; mocking
  the database layer is prohibited.
- Every user-facing endpoint MUST have at least one integration test that
  exercises the full request-response path including the database.

**Rationale**: Testcontainers infrastructure is already configured.
Integration tests validate real behavior across layers and catch regressions
that unit tests miss. A single test type reduces maintenance overhead and
eliminates redundant coverage between unit and integration layers.

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
  cases demand it. **Exception**: hexagonal architecture boundaries
  (ports and adapters) are the one permitted upfront abstraction because
  they enforce dependency direction from project inception; all other
  abstractions still require two concrete use cases.
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
- **Architecture**: Hexagonal (Ports & Adapters). The codebase MUST be
  organized into two layers under `src/main/java/dev/abbah/spring/dot/`:

  ```text
  src/main/java/dev/abbah/spring/dot/
  ├── domain/
  │   └── <domain_name>/
  │       ├── <Domain>.java              # Domain object (record)
  │       ├── <UseCase>.java             # Business logic (@Service)
  │       └── <Port>.java                # Port interface (driven)
  └── infra/
      ├── spi/                           # Driven adapters (outbound)
      │   ├── db/<domain_name>/
      │   │   ├── <Entity>.java          # Persistence entity (record)
      │   │   ├── <Mapper>.java          # MapStruct (entity ↔ domain)
      │   │   └── <Adapter>.java         # Implements Port
      │   └── messaging/<domain_name>/
      │       ├── <Producer>.java        # Event producer (implements Port)
      │       ├── <Event>.java           # Outbound event (record)
      │       └── <Mapper>.java          # MapStruct (event ↔ domain)
      └── api/                           # Driving adapters (inbound)
          ├── rest/<domain_name>/
          │   ├── <Resource>.java        # REST controller
          │   ├── <Mapper>.java          # MapStruct (DTO ↔ domain)
          │   └── <Dto>.java             # Request/response DTOs
          └── messaging/<domain_name>/
              ├── <Consumer>.java        # Event consumer
              ├── <Event>.java           # Inbound event (record)
              └── <Mapper>.java          # MapStruct (event ↔ domain)
  ```

  - **Domain** (`domain/<domain_name>/`): Pure business logic with no
    infrastructure dependencies. Each domain package MUST contain:
    - `<Domain>.java` — Domain objects (Java record when possible).
    - `<UseCase>.java` — Business logic (`@Service`).
    - `<Port>.java` — Port interface (driven/outbound).
  - **Infra** (`infra/`): All infrastructure adapters, split by direction.
    New adapter technologies MUST follow the naming pattern
    `infra/spi/<technology>/<domain_name>/` (driven/outbound) or
    `infra/api/<protocol>/<domain_name>/` (driving/inbound):
    - `infra/spi/db/<domain_name>/` — Database (driven/outbound):
      - `<Entity>.java` — Persistence entities (Java record).
      - `<Mapper>.java` — MapStruct mapper (entity ↔ domain).
      - `<Adapter>.java` — Database adapter implementing the Port.
    - `infra/spi/messaging/<domain_name>/` — Messaging producer
      (driven/outbound, e.g., Kafka):
      - `<Producer>.java` — Event producer implementing the Port.
      - `<Event>.java` — Outbound event payload (Java record).
      - `<Mapper>.java` — MapStruct mapper (event ↔ domain).
    - `infra/api/rest/<domain_name>/` — REST (driving/inbound):
      - `<Resource>.java` — REST controller.
      - `<Mapper>.java` — MapStruct mapper (DTO ↔ domain).
      - `<Dto>.java` — Request/response DTOs.
    - `infra/api/messaging/<domain_name>/` — Messaging consumer
      (driving/inbound, e.g., Kafka):
      - `<Consumer>.java` — Event consumer (driving adapter).
      - `<Event>.java` — Inbound event payload (Java record).
      - `<Mapper>.java` — MapStruct mapper (event ↔ domain).
  - `<domain_name>` MUST be the feature domain name in lowercase
    (e.g., `checklist`).
  - Dependencies MUST flow inward: `infra → domain`. The domain layer
    MUST NEVER import from infra.
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
  Integration tests only (see Principle II).

## Development Workflow

- **Branching**: Feature branches MUST branch from `main` and be merged back
  via pull request. Direct commits to `main` are prohibited.
- **Commit Messages**: Follow Conventional Commits format
  (`type: description`, e.g., `feat: add user registration endpoint`).
- **Pull Requests**: Every PR MUST include a description of what changed and
  why. PRs MUST pass all automated tests before merge.
- **Testing Gate**: CI MUST run the full integration test suite (via
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

**Version**: 3.0.0 | **Ratified**: 2026-02-21 | **Last Amended**: 2026-02-21
