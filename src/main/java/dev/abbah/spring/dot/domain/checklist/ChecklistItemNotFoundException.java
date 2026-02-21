package dev.abbah.spring.dot.domain.checklist;

public class ChecklistItemNotFoundException extends RuntimeException {

    public ChecklistItemNotFoundException(Long id) {
        super("Checklist item not found with id: " + id);
    }
}
