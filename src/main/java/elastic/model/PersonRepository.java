package elastic.model;

import java.util.Collection;
import java.util.Optional;

public interface PersonRepository {
    void save(Person person);
    Optional<Person> get(String id);
    Collection<Person> getAll();
}
