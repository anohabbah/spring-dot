package dev.abbah.spring.dot.infra.spi.db.checklist;

import dev.abbah.spring.dot.domain.checklist.ChecklistItem;
import dev.abbah.spring.dot.domain.checklist.ChecklistItemPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChecklistItemAdapter implements ChecklistItemPort {

    private final ChecklistItemRepository repository;
    private final ChecklistItemDbMapper mapper;

    @Override
    @Transactional
    public ChecklistItem save(ChecklistItem item) {
        var entity = mapper.toEntity(item);
        var saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<ChecklistItem> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<ChecklistItem> findAll() {
        return repository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return repository.existsById(id);
    }
}
