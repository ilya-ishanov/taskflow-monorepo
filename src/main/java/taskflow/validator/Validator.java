package taskflow.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import taskflow.exceptions.EntityNotFoundException;

@Component
@RequiredArgsConstructor
public class Validator {
    public <T> void validateEntityExists(Long id, JpaRepository<T, Long> repository, String entityName) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException(entityName, id);
        }
    }
}
