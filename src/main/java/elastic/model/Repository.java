package elastic.model;

import java.util.Collection;
import java.util.Optional;

public interface Repository<T> {
    String save(String id, T value);
    Optional<T> get(String id);
    Collection<T> getAll();
    void deleteIndex();
}
