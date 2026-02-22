# Implementation Plan: CRUD Checklist Items

**Branch**: `001-crud-checklist-items` | **Date**: 2026-02-21 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/001-crud-checklist-items/spec.md`

## Summary

Build a CRUD REST API for checklist items. Users can create, read, update, and delete checklist items, each having a
name and checked/unchecked status. The API follows RESTful conventions with Spring Boot 4's path-segment versioning,
persists data in PostgreSQL via Spring Data JDBC, and uses the project's hexagonal architecture with domain, SPI (
database adapter), and API (REST controller) layers.

## Technical Context

**Language/Version**: Java 21 (LTS)
**Primary Dependencies**: Spring Boot 4.0.3 (WebMVC, Data JDBC, Flyway, Actuator), MapStruct 1.7.0.Beta1, Lombok 9.2.0,
SpringDoc OpenAPI 3.0.1
**Storage**: PostgreSQL 18 via Spring Data JDBC + Flyway migrations
**Testing**: JUnit 6 (Jupiter 6) + Spring Boot Test + MockMvcTester + Testcontainers (PostgreSQL)
**Target Platform**: Server-side REST API (JVM)
**Project Type**: Single Spring Boot application (hexagonal architecture)
**Performance Goals**: Responses within 2 seconds (per SC-001, SC-002)
**Constraints**: No authentication, no pagination, max 255 char item names
**Scale/Scope**: Single entity (ChecklistItem), 5 REST endpoints, single Flyway migration

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle                          | Status | Evidence                                                                                                                                                                                                                                                                              |
|------------------------------------|--------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| I. API-First Design                | PASS   | OpenAPI contract defined in `contracts/openapi.yaml` before implementation. SpringDoc annotations on controller. DTOs separate from domain/entity records. Path-segment versioning via `application.yaml` with `spring.mvc.apiversion`. Version attribute on all mapping annotations. |
| II. Test-Driven Development        | PASS   | Integration tests only, using `@SpringBootTest` + `@AutoConfigureMockMvc` + `@Import(TestcontainersConfiguration.class)` + `MockMvcTester`. Red-Green-Refactor cycle. Every endpoint tested against real PostgreSQL via Testcontainers.                                               |
| III. Database Migration Discipline | PASS   | Single Flyway migration `V1__create_checklist_item_table.sql` in `src/main/resources/db/migration/`. Additive-only. Tested via Testcontainers.                                                                                                                                        |
| IV. Simplicity & YAGNI             | PASS   | Single entity, no premature abstractions beyond hexagonal boundaries. Spring Data JDBC only. No new dependencies needed. Configuration in `application.yaml`.                                                                                                                         |
| V. Observability                   | PASS   | Use case service logs side effects (create, update, delete) at INFO level with structured key-value pairs. Error paths (not found, validation) logged at WARN.                                                                                                                        |

**Gate result**: ALL PASS — no violations, no complexity tracking needed.

**Post-Phase 1 re-check**: ALL PASS — design artifacts align with constitution. No new violations introduced.

## Project Structure

### Documentation (this feature)

```text
specs/001-crud-checklist-items/
├── spec.md              # Feature specification
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output
│   └── openapi.yaml     # OpenAPI 3.1 contract
└── tasks.md             # Phase 2 output (/speckit.tasks command)
```

### Source Code (repository root)

```text
src/main/java/dev/abbah/spring/dot/
├── domain/
│   └── checklist/
│       ├── ChecklistItem.java          # Domain record (id, name, checked)
│       ├── ChecklistItemUseCase.java   # Business logic (@Service)
│       └── ChecklistItemPort.java      # Port interface (driven/outbound)
└── infra/
    ├── spi/
    │   └── db/checklist/
    │       ├── ChecklistItemEntity.java    # Persistence record (@Table)
    │       ├── ChecklistItemDbMapper.java  # MapStruct (entity ↔ domain)
    │       └── ChecklistItemAdapter.java   # Implements ChecklistItemPort
    └── api/
        └── rest/checklist/
            ├── ChecklistItemResource.java      # REST controller (@RestController)
            ├── ChecklistItemApiMapper.java      # MapStruct (DTO ↔ domain)
            ├── CreateChecklistItemDto.java  # Request DTO (create)
            ├── UpdateChecklistItemDto.java  # Request DTO (update)
            └── ChecklistItemDto.java       # Response DTO

src/main/resources/
├── application.yaml                              # App config + API versioning
└── db/migration/
    └── V1__create_checklist_item_table.sql       # Flyway migration

src/test/java/dev/abbah/spring/dot/
└── infra/api/rest/checklist/
    └── ChecklistItemResourceTest.java            # Integration tests
```

**Structure Decision**: Hexagonal architecture as mandated by the constitution. Domain package `checklist` contains the
pure business record, use case service, and port interface. Infrastructure layer has a database adapter (SPI) and REST
controller (API). No messaging adapters needed for this feature.

## Complexity Tracking

No constitution violations. Table not applicable.
