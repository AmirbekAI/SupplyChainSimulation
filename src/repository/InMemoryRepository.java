package repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryRepository <T> implements InMemoryRepositoryInterface<T> {
    private final Map<UUID, T> storage;

    public InMemoryRepository() {
        this.storage = new ConcurrentHashMap<>();
    }
    @Override
    public T save(UUID id, T entity) {
         storage.put(id, entity);
         return entity;
     }

     public Optional<T> findById(UUID id) {
        return Optional.ofNullable(storage.get(id));
     }

     public List<T> findAll() {
        return new ArrayList<>(storage.values());
     }

     public void deleteById(UUID id) {
        storage.remove(id);
     }

     public boolean existsById(UUID id) {
        return storage.containsKey(id);
     }
}
