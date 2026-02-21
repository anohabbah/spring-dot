package dev.abbah.spring.dot.infra.api.rest.checklist;

import dev.abbah.spring.dot.domain.checklist.ChecklistItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface ChecklistItemApiMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "checked", constant = "false")
    ChecklistItem toDomain(CreateChecklistItemDto dto);

    @Mapping(target = "id", ignore = true)
    ChecklistItem toDomain(UpdateChecklistItemDto dto);

    ChecklistItemDto toDto(ChecklistItem domain);
}
