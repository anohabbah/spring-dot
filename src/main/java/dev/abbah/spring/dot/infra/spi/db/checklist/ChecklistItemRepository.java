package dev.abbah.spring.dot.infra.spi.db.checklist;

import org.springframework.data.repository.ListCrudRepository;

public interface ChecklistItemRepository extends ListCrudRepository<ChecklistItemEntity, Long> {
}
