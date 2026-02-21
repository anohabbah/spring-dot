package dev.abbah.spring.dot.infra.api.rest.checklist;

import dev.abbah.spring.dot.domain.checklist.ChecklistItemUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Checklist Items", description = "CRUD operations for checklist items")
@RestController
@RequestMapping("/{version}/checklist")
@RequiredArgsConstructor
public class ChecklistItemResource {

    private final ChecklistItemUseCase useCase;
    private final ChecklistItemApiMapper mapper;

    @Operation(summary = "Create a new checklist item")
    @ApiResponse(responseCode = "201", description = "Checklist item created successfully")
    @ApiResponse(responseCode = "400", description = "Validation error")
    @PostMapping(version = "1.0")
    public ResponseEntity<ChecklistItemDto> create(@Valid @RequestBody CreateChecklistItemDto dto) {
        var item = mapper.toDomain(dto);
        var created = useCase.create(item);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDto(created));
    }

    @Operation(summary = "List all checklist items")
    @ApiResponse(responseCode = "200", description = "List of all checklist items")
    @GetMapping(version = "1.0")
    public List<ChecklistItemDto> getAll() {
        return useCase.findAll().stream().map(mapper::toDto).toList();
    }

    @Operation(summary = "Get a checklist item by ID")
    @ApiResponse(responseCode = "200", description = "Checklist item found")
    @ApiResponse(responseCode = "404", description = "Checklist item not found")
    @GetMapping(version = "1.0", path = "/{id}")
    public ResponseEntity<ChecklistItemDto> getById(@PathVariable Long id) {
        return ResponseEntity.of(useCase.findById(id).map(mapper::toDto));
    }

    @Operation(summary = "Update a checklist item")
    @ApiResponse(responseCode = "200", description = "Checklist item updated successfully")
    @ApiResponse(responseCode = "400", description = "Validation error")
    @ApiResponse(responseCode = "404", description = "Checklist item not found")
    @PutMapping(version = "1.0", path = "/{id}")
    public ChecklistItemDto update(@PathVariable Long id, @Valid @RequestBody UpdateChecklistItemDto dto) {
        var item = mapper.toDomain(dto);
        var updated = useCase.update(id, item);
        return mapper.toDto(updated);
    }

    @Operation(summary = "Delete a checklist item")
    @ApiResponse(responseCode = "204", description = "Checklist item deleted successfully")
    @ApiResponse(responseCode = "404", description = "Checklist item not found")
    @DeleteMapping(version = "1.0", path = "/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        useCase.delete(id);
        return ResponseEntity.noContent().build();
    }
}
