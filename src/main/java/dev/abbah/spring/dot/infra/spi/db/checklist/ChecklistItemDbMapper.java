package dev.abbah.spring.dot.infra.spi.db.checklist;

import dev.abbah.spring.dot.domain.checklist.ChecklistItem;
import org.mapstruct.Mapper;

@Mapper
public interface ChecklistItemDbMapper {

    ChecklistItemEntity toEntity(ChecklistItem domain);

    ChecklistItem toDomain(ChecklistItemEntity entity);
}
