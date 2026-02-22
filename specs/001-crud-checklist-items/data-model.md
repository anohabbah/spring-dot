# Data Model: CRUD Checklist Items

**Feature**: 001-crud-checklist-items
**Date**: 2026-02-21

## Entities

### ChecklistItem

Represents a single task on a user's checklist.

| Field   | Type                 | Constraints                        | Description                        |
|---------|----------------------|------------------------------------|------------------------------------|
| id      | Long (BIGSERIAL)     | Primary key, auto-generated        | Unique identifier                  |
| name    | String (VARCHAR 255) | NOT NULL, not blank, max 255 chars | Display name of the checklist item |
| checked | boolean (BOOLEAN)    | NOT NULL, default FALSE            | Whether the item is marked as done |

### Relationships

None. ChecklistItem is a standalone aggregate root with no relationships to other entities.

### Validation Rules

| Field   | Rule                                         | Source |
|---------|----------------------------------------------|--------|
| name    | Must not be null                             | FR-010 |
| name    | Must not be blank (empty or whitespace-only) | FR-010 |
| name    | Must not exceed 255 characters               | FR-011 |
| checked | Defaults to false on creation                | FR-003 |

### State Transitions

```text
[Created] ---> unchecked (checked = false)
                  |
                  v
              checked (checked = true)
                  |
                  v
              unchecked (checked = false)   ← toggleable
                  |
                  v
              [Deleted]                     ← permanent removal
```

Items can freely toggle between checked and unchecked. Deletion is permanent and irreversible.

## Database Schema

### Table: checklist_item

```sql
CREATE TABLE checklist_item
(
    id      BIGSERIAL PRIMARY KEY,
    name    VARCHAR(255) NOT NULL,
    checked BOOLEAN      NOT NULL DEFAULT FALSE
);
```

### Indexes

None beyond the primary key index. Per YAGNI (Constitution Principle IV), additional indexes will be added only when
query patterns or performance data justify them.

## Layer Mapping

### Domain Record

```text
ChecklistItem(Long id, String name, boolean checked)
```

### Persistence Entity

```text
ChecklistItemEntity(@Id Long id, @Column String name, @Column boolean checked)
  + withId(Long id) wither for post-insert ID population
```

### Request DTOs

```text
CreateChecklistItemDto(String name)
  - checked defaults to false, not provided by client

UpdateChecklistItemDto(String name, boolean checked)
  - both fields required (full replacement via PUT)
```

### Response DTO

```text
ChecklistItemDto(Long id, String name, boolean checked)
```
