package repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InMemoryRepositoryInterface<T> {
    T save (UUID id, T entity);
    Optional<T> findById(UUID id);
    List<T> findAll();
    void deleteById(UUID id);
    boolean existsById(UUID id);
}
