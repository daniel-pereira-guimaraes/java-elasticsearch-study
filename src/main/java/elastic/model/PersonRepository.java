package elastic.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PersonRepository {
    void save(Person person);
    Optional<Person> get(String id);
    List<Person> getAll(boolean onlyCustomers);
    List<Person> queryByName(String name);
    List<Person> queryByCreditLimit(BigDecimal minValue, BigDecimal maxValue);
    void createIndex();
    void deleteIndex();
}
