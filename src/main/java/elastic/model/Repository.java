package elastic.model;

import java.util.Map;
import java.util.Optional;

public interface Repository<T> {
    String save(String id, T value);
    Optional<T> get(String id);
    Map<String, T> getAll();
    Map<String, T> queryByName(String name);
    void deleteIndex();
}
