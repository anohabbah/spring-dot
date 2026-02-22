package dev.abbah.spring.dot.domain.checklist;

import java.util.List;
import java.util.Optional;

public interface ChecklistItemPort {

    ChecklistItem save(ChecklistItem item);

    Optional<ChecklistItem> findById(Long id);

    List<ChecklistItem> findAll();

    void deleteById(Long id);

    boolean existsById(Long id);
}
