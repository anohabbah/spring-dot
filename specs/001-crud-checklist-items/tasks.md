# Tasks: CRUD Checklist Items

**Input**: Design documents from `/specs/001-crud-checklist-items/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/openapi.yaml

**Tests**: Required — constitution Principle II mandates TDD (Red-Green-Refactor) with integration tests only.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3, US4)
- Include exact file paths in descriptions

## Path Conventions

All source paths are relative to repository root under the hexagonal architecture:

- **Domain**: `src/main/java/dev/abbah/spring/dot/domain/checklist/`
- **Persistence SPI**: `src/main/java/dev/abbah/spring/dot/infra/spi/db/checklist/`
- **REST API**: `src/main/java/dev/abbah/spring/dot/infra/api/rest/checklist/`
- **Resources**: `src/main/resources/`
- **Tests**: `src/test/java/dev/abbah/spring/dot/infra/api/rest/checklist/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Application configuration and database schema

- [ ] T001 Update `src/main/resources/application.yaml` to add Spring Boot 4 path-segment API versioning (
  `spring.mvc.apiversion` with `use.path-segment: 0`, `supported: 1.0`, `default: 1.0`) and PostgreSQL datasource
  configuration
- [ ] T002 [P] Create Flyway migration `src/main/resources/db/migration/V1__create_checklist_item_table.sql` with
  `checklist_item` table (id BIGSERIAL PRIMARY KEY, name VARCHAR(255) NOT NULL, checked BOOLEAN NOT NULL DEFAULT FALSE)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Domain model, persistence layer, and REST DTOs that ALL user stories depend on

**CRITICAL**: No user story work can begin until this phase is complete

- [ ] T003 [P] Create `ChecklistItem` domain record (Long id, String name, boolean checked) in
  `src/main/java/dev/abbah/spring/dot/domain/checklist/ChecklistItem.java`
- [ ] T004 [P] Create `ChecklistItemPort` interface with methods: save(ChecklistItem), findById(Long), findAll(),
  deleteById(Long), existsById(Long) in `src/main/java/dev/abbah/spring/dot/domain/checklist/ChecklistItemPort.java`
- [ ] T005 [P] Create `ChecklistItemNotFoundException` extending RuntimeException in
  `src/main/java/dev/abbah/spring/dot/domain/checklist/ChecklistItemNotFoundException.java`
- [ ] T006 Create persistence layer in `src/main/java/dev/abbah/spring/dot/infra/spi/db/checklist/`:
  `ChecklistItemEntity` record with @Table, @Id, withId() wither; `ChecklistItemRepository` extending
  ListCrudRepository; `ChecklistItemDbMapper` MapStruct interface (entity ↔ domain); `ChecklistItemAdapter` @Component
  implementing ChecklistItemPort
- [ ] T007 Create REST DTOs and mapper in `src/main/java/dev/abbah/spring/dot/infra/api/rest/checklist/`:
  `CreateChecklistItemDto` record (String name with @NotBlank @Size(max=255)); `UpdateChecklistItemDto` record (String
  name with @NotBlank @Size(max=255), boolean checked); `ChecklistItemDto` response record (Long id, String name,
  boolean checked); `ChecklistItemApiMapper` MapStruct interface (DTO ↔ domain)

**Checkpoint**: Foundation ready — all data structures, persistence layer, and DTOs compiled. User story TDD cycles can
now begin.

---

## Phase 3: User Story 1 — Create a Checklist Item (Priority: P1) MVP

**Goal**: Users can create a new checklist item by providing a name. The system assigns a unique ID, defaults checked to
false, persists it, and returns the created item.

**Independent Test**: POST /v1/checklist with `{"name": "Buy groceries"}` returns 201 with id, name, and
checked=false. Blank/missing name returns 400.

### TDD Cycle for User Story 1

> **Write tests FIRST, ensure they FAIL, then implement to make them pass**

- [ ] T008 [US1] Write failing integration tests for POST /v1/checklist in
  `src/test/java/dev/abbah/spring/dot/infra/api/rest/checklist/ChecklistItemResourceTest.java`: test creating item
  returns 201 with id + name + checked=false; test creating second item gets its own id; test blank name returns 400;
  test missing name returns 400. Use MockMvcTester with @SpringBootTest + @AutoConfigureMockMvc + @Import(
  TestcontainersConfiguration.class)
- [ ] T009 [US1] Implement `create(ChecklistItem)` method in `ChecklistItemUseCase` @Service in
  `src/main/java/dev/abbah/spring/dot/domain/checklist/ChecklistItemUseCase.java` — delegates to
  ChecklistItemPort.save(), logs at INFO level with structured key-value pairs
- [ ] T010 [US1] Implement POST endpoint (version="1.0") with @Valid @RequestBody CreateChecklistItemDto in
  `ChecklistItemResource` @RestController at `/{version}/checklist` in
  `src/main/java/dev/abbah/spring/dot/infra/api/rest/checklist/ChecklistItemResource.java` — returns 201 CREATED with
  ChecklistItemDto response

**Checkpoint**: POST /v1/checklist works end-to-end. All T008 tests pass (GREEN).

---

## Phase 4: User Story 2 — View Checklist Items (Priority: P1)

**Goal**: Users can retrieve all checklist items as a list, or a single item by ID. Non-existent IDs return 404.

**Independent Test**: GET /v1/checklist returns 200 with array. GET /v1/checklist/{id} returns 200 with item or
404 for non-existent ID.

### TDD Cycle for User Story 2

- [ ] T011 [US2] Write failing integration tests for GET endpoints in
  `src/test/java/dev/abbah/spring/dot/infra/api/rest/checklist/ChecklistItemResourceTest.java`: test GET
  /v1/checklist returns 200 with list of items; test empty list returns 200 with empty array; test GET
  /v1/checklist/{id} returns 200 with item; test GET /v1/checklist/{id} for non-existent ID returns 404
- [ ] T012 [US2] Implement `findAll()` returning List<ChecklistItem> and `findById(Long)` returning ChecklistItem (
  throws ChecklistItemNotFoundException if not found) in `ChecklistItemUseCase` in
  `src/main/java/dev/abbah/spring/dot/domain/checklist/ChecklistItemUseCase.java`
- [ ] T013 [US2] Implement GET all (version="1.0") and GET by ID (version="1.0", path="/{id}") endpoints in
  `ChecklistItemResource` in `src/main/java/dev/abbah/spring/dot/infra/api/rest/checklist/ChecklistItemResource.java` —
  add @ExceptionHandler for ChecklistItemNotFoundException returning 404 ProblemDetail

**Checkpoint**: GET endpoints work end-to-end. All T011 tests pass (GREEN). US1 tests still pass.

---

## Phase 5: User Story 3 — Update a Checklist Item (Priority: P2)

**Goal**: Users can update an existing checklist item's name and/or checked status via PUT. Non-existent IDs return 404.
Invalid input returns 400.

**Independent Test**: PUT /v1/checklist/{id} with `{"name": "Updated", "checked": true}` returns 200 with updated
item. Non-existent ID returns 404. Blank name returns 400.

### TDD Cycle for User Story 3

- [ ] T014 [US3] Write failing integration tests for PUT /v1/checklist/{id} in
  `src/test/java/dev/abbah/spring/dot/infra/api/rest/checklist/ChecklistItemResourceTest.java`: test updating name
  returns 200 with new name; test updating checked status returns 200 with checked=true; test non-existent ID returns
  404; test blank name returns 400
- [ ] T015 [US3] Implement `update(Long id, ChecklistItem)` in `ChecklistItemUseCase` in
  `src/main/java/dev/abbah/spring/dot/domain/checklist/ChecklistItemUseCase.java` — verify existence via port, save
  updated item, log at INFO level; throw ChecklistItemNotFoundException if not found
- [ ] T016 [US3] Implement PUT endpoint (version="1.0", path="/{id}") with @Valid @RequestBody UpdateChecklistItemDto in
  `ChecklistItemResource` in `src/main/java/dev/abbah/spring/dot/infra/api/rest/checklist/ChecklistItemResource.java` —
  returns 200 with ChecklistItemDto response

**Checkpoint**: PUT endpoint works end-to-end. All T014 tests pass (GREEN). US1 and US2 tests still pass.

---

## Phase 6: User Story 4 — Delete a Checklist Item (Priority: P2)

**Goal**: Users can permanently delete a checklist item by ID. Non-existent IDs return 404. Deleted items are no longer
retrievable.

**Independent Test**: DELETE /v1/checklist/{id} returns 204. Subsequent GET for same ID returns 404. Non-existent ID
returns 404.

### TDD Cycle for User Story 4

- [ ] T017 [US4] Write failing integration tests for DELETE /v1/checklist/{id} in
  `src/test/java/dev/abbah/spring/dot/infra/api/rest/checklist/ChecklistItemResourceTest.java`: test deleting existing
  item returns 204; test deleted item is no longer retrievable via GET (404); test deleting non-existent ID returns 404
- [ ] T018 [US4] Implement `delete(Long id)` in `ChecklistItemUseCase` in
  `src/main/java/dev/abbah/spring/dot/domain/checklist/ChecklistItemUseCase.java` — verify existence via port, delete,
  log at INFO level; throw ChecklistItemNotFoundException if not found
- [ ] T019 [US4] Implement DELETE endpoint (version="1.0", path="/{id}") in `ChecklistItemResource` in
  `src/main/java/dev/abbah/spring/dot/infra/api/rest/checklist/ChecklistItemResource.java` — returns 204 NO_CONTENT

**Checkpoint**: DELETE endpoint works end-to-end. All T017 tests pass (GREEN). All previous story tests still pass.

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Observability, documentation, and edge case coverage

- [ ] T020 [P] Add SpringDoc OpenAPI annotations (@Operation, @ApiResponse, @Schema, @Tag) to all endpoints in
  `src/main/java/dev/abbah/spring/dot/infra/api/rest/checklist/ChecklistItemResource.java`
- [ ] T021 [P] Add structured WARN logging for error paths (not found, validation failure) in `ChecklistItemUseCase` in
  `src/main/java/dev/abbah/spring/dot/domain/checklist/ChecklistItemUseCase.java`
- [ ] T022 Write edge case integration tests in
  `src/test/java/dev/abbah/spring/dot/infra/api/rest/checklist/ChecklistItemResourceTest.java`: test name exceeding 255
  characters returns 400; test request body with extra/unexpected fields is handled gracefully

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — can start immediately
- **Foundational (Phase 2)**: Depends on Phase 1 completion — BLOCKS all user stories
- **User Stories (Phases 3–6)**: All depend on Phase 2 completion
    - US1 and US2 are both P1 but US2 depends on US1 (needs created items to read)
    - US3 depends on US1 (needs created items to update)
    - US4 depends on US1 (needs created items to delete)
- **Polish (Phase 7)**: Depends on all user stories being complete

### User Story Dependencies

- **US1 — Create (P1)**: Can start after Phase 2 — no dependency on other stories
- **US2 — View (P1)**: Can start after Phase 2 — tests use POST from US1 to seed data, so US1 must be complete
- **US3 — Update (P2)**: Can start after US1 — tests use POST to seed data, then PUT to update
- **US4 — Delete (P2)**: Can start after US1 — tests use POST to seed data, then DELETE to remove

### Within Each User Story (TDD Cycle)

1. Write integration test(s) — must FAIL (RED)
2. Implement use case method — domain layer
3. Implement controller endpoint — infrastructure layer
4. Verify tests PASS (GREEN)
5. Refactor if needed

### Parallel Opportunities

- T001 and T002 in Phase 1 can run in parallel
- T003, T004, T005 in Phase 2 can run in parallel (independent domain files)
- T006 and T007 in Phase 2 can run in parallel (different packages, both depend on T003)
- T020 and T021 in Phase 7 can run in parallel (different files)
- US3 and US4 can run in parallel (both depend only on US1, touch different methods)

---

## Parallel Example: Phase 2 Foundational

```text
# Launch domain records in parallel:
Task: "Create ChecklistItem record in domain/checklist/ChecklistItem.java"
Task: "Create ChecklistItemPort interface in domain/checklist/ChecklistItemPort.java"
Task: "Create ChecklistItemNotFoundException in domain/checklist/ChecklistItemNotFoundException.java"

# Then launch persistence and REST layers in parallel:
Task: "Create persistence layer (entity, repo, mapper, adapter) in infra/spi/db/checklist/"
Task: "Create REST DTOs and mapper (create, update, response DTOs, API mapper) in infra/api/rest/checklist/"
```

## Parallel Example: User Stories 3 & 4

```text
# After US1 is complete, US3 and US4 can proceed in parallel:
# Developer A:
Task: "T014 [US3] Write failing tests for PUT"
Task: "T015 [US3] Implement update in use case"
Task: "T016 [US3] Implement PUT endpoint"

# Developer B (simultaneously):
Task: "T017 [US4] Write failing tests for DELETE"
Task: "T018 [US4] Implement delete in use case"
Task: "T019 [US4] Implement DELETE endpoint"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (config + migration)
2. Complete Phase 2: Foundational (domain model + persistence + DTOs)
3. Complete Phase 3: User Story 1 — Create (TDD cycle)
4. **STOP and VALIDATE**: Run `./gradlew test` — all tests pass
5. Users can create checklist items via POST — MVP delivered

### Incremental Delivery

1. Setup + Foundational → Foundation ready
2. Add US1 (Create) → Test → Deploy (MVP!)
3. Add US2 (View) → Test → Deploy (users can see their items)
4. Add US3 (Update) → Test → Deploy (users can modify items)
5. Add US4 (Delete) → Test → Deploy (full CRUD complete)
6. Polish → Test → Deploy (production-ready)

### Single Developer Strategy

Execute sequentially: Phase 1 → Phase 2 → US1 → US2 → US3 → US4 → Polish

Each story follows strict Red-Green-Refactor. Commit after each GREEN checkpoint.

---

## Notes

- [P] tasks = different files, no dependencies on incomplete tasks
- [Story] label maps task to specific user story for traceability
- TDD is NON-NEGOTIABLE per constitution Principle II — every endpoint must have integration tests
- All tests use @SpringBootTest + @AutoConfigureMockMvc + @Import(TestcontainersConfiguration.class) + MockMvcTester
- API path is `/{version}/checklist` with path-segment versioning (version="1.0")
- Commit after each completed TDD cycle (story checkpoint)
- Run full test suite (`./gradlew test`) at each checkpoint to verify no regressions
