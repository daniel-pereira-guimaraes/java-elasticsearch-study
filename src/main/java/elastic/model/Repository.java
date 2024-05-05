package elastic.model;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

public interface Repository<T> {
    String save(String id, T value);
    Optional<T> get(String id);
    Map<String, T> getAll();
    Map<String, T> queryByName(String name);
    Map<String, T> queryByCreditLimit(BigDecimal minValue, BigDecimal maxValue);
    void createIndex();
    void deleteIndex();
}
