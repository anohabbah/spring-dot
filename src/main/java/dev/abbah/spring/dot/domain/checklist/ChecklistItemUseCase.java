package dev.abbah.spring.dot.domain.checklist;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChecklistItemUseCase {

    private final ChecklistItemPort port;

    public ChecklistItem create(ChecklistItem item) {
        var created = port.save(item);
        log.info("action=create id={} name=\"{}\"", created.id(), created.name());
        return created;
    }

    public List<ChecklistItem> findAll() {
        return port.findAll();
    }

    public Optional<ChecklistItem> findById(Long id) {
        return port.findById(id);
    }

    public ChecklistItem update(Long id, ChecklistItem item) {
        if (!port.existsById(id)) {
            log.warn("action=update status=not_found id={}", id);
            throw new ChecklistItemNotFoundException(id);
        }
        var updated = port.save(new ChecklistItem(id, item.name(), item.checked()));
        log.info("action=update id={} name=\"{}\" checked={}", updated.id(), updated.name(), updated.checked());
        return updated;
    }

    public void delete(Long id) {
        if (!port.existsById(id)) {
            log.warn("action=delete status=not_found id={}", id);
            throw new ChecklistItemNotFoundException(id);
        }
        port.deleteById(id);
        log.info("action=delete id={}", id);
    }
}
