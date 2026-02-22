CREATE TABLE checklist_item
(
    id      BIGSERIAL PRIMARY KEY,
    name    VARCHAR(255) NOT NULL,
    checked BOOLEAN      NOT NULL DEFAULT FALSE
);
