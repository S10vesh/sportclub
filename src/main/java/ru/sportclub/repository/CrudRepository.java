package ru.sportclub.repository;

import java.util.List;
import java.util.Optional;

public interface CrudRepository<T> {
    T save(T entity);
    List<T> findAll();
    Optional<T> findById(long id);
    T update(T entity);
    void deleteById(long id);
}
