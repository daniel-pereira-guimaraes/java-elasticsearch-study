package elastic.model;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface Repository<T> {
    String save(String id, T value);
    Optional<T> get(String id);
    Map<String, T> getAll();
    void deleteIndex();
}
