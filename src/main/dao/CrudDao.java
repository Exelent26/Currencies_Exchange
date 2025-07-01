package main.dao;

import java.util.List;
import java.util.Optional;

public interface CrudDao<T> {
    Optional<T> findById(int id);
    List<T> findAll();
    T save(T entity);

}
