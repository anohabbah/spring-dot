# Quickstart: CRUD Checklist Items

**Feature**: 001-crud-checklist-items
**Date**: 2026-02-21

## Prerequisites

- Java 21+ installed
- Docker running (for Testcontainers / PostgreSQL)
- Gradle wrapper available (`./gradlew`)

## Run the Application (dev mode with Testcontainers)

```bash
./gradlew bootTestRun
```

This starts the app using `TestSpringDotApplication` which automatically provisions a PostgreSQL 18 container via
Testcontainers.

## Run Tests

```bash
./gradlew test
```

All tests are integration tests running against a real PostgreSQL instance via Testcontainers.

## API Endpoints

Base URL: `http://localhost:8080/api/v1`

| Method | Path                    | Description    | Request Body                       | Response      |
|--------|-------------------------|----------------|------------------------------------|---------------|
| GET    | `/checklist`      | List all items | —                                  | `200` + array |
| POST   | `/checklist`      | Create an item | `{"name": "..."}`                  | `201` + item  |
| GET    | `/checklist/{id}` | Get item by ID | —                                  | `200` + item  |
| PUT    | `/checklist/{id}` | Update an item | `{"name": "...", "checked": true}` | `200` + item  |
| DELETE | `/checklist/{id}` | Delete an item | —                                  | `204`         |

## Example Requests

### Create a checklist item

```bash
curl -X POST http://localhost:8080/api/v1/checklist \
  -H "Content-Type: application/json" \
  -d '{"name": "Buy groceries"}'
```

Response (`201 Created`):

```json
{
  "id": 1,
  "name": "Buy groceries",
  "checked": false
}
```

### List all checklist items

```bash
curl http://localhost:8080/api/v1/checklist
```

Response (`200 OK`):

```json
[
  {
    "id": 1,
    "name": "Buy groceries",
    "checked": false
  }
]
```

### Get a checklist item by ID

```bash
curl http://localhost:8080/api/v1/checklist/1
```

Response (`200 OK`):

```json
{
  "id": 1,
  "name": "Buy groceries",
  "checked": false
}
```

### Update a checklist item

```bash
curl -X PUT http://localhost:8080/api/v1/checklist/1 \
  -H "Content-Type: application/json" \
  -d '{"name": "Buy organic groceries", "checked": true}'
```

Response (`200 OK`):

```json
{
  "id": 1,
  "name": "Buy organic groceries",
  "checked": true
}
```

### Delete a checklist item

```bash
curl -X DELETE http://localhost:8080/api/v1/checklist/1
```

Response: `204 No Content`

## API Documentation

When the application is running, OpenAPI documentation is available at:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Key Architecture Notes

- **Hexagonal architecture**: Domain logic in `domain/checklist/`, infrastructure adapters in `infra/`
- **Spring Data JDBC**: No JPA/Hibernate. Direct SQL mapping with record-based entities.
- **Flyway**: Schema managed via `V1__create_checklist_item_table.sql`
- **MapStruct**: Mapping between domain records, persistence entities, and DTOs
- **Path-segment versioning**: `/api/v1/...` configured via `spring.mvc.apiversion` in `application.yaml`
