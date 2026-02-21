package dev.abbah.spring.dot.infra.spi.db.checklist;

import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@With
@Table("checklist_item")
public record ChecklistItemEntity(@Id Long id, String name, boolean checked) {
}
