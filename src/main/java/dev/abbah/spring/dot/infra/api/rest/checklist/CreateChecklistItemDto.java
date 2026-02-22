package dev.abbah.spring.dot.infra.api.rest.checklist;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateChecklistItemDto(@NotBlank @Size(max = 255) String name) {
}
