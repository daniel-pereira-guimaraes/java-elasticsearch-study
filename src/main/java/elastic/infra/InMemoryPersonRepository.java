package elastic.infra;

import elastic.model.Person;
import elastic.model.PersonRepository;

import java.util.*;

public class InMemoryPersonRepository implements PersonRepository {

    private final Map<String, Person> map = new HashMap<>();

    @Override
    public void save(Person person) {
        if (person.id() == null) {
            person.initialize(UUID.randomUUID().toString());
        }
        map.put(person.id(), person);
    }

    @Override
    public Optional<Person> get(String id) {
        var person = map.get(id);
        return person == null ? Optional.empty() : Optional.of(person);
    }

    @Override
    public Collection<Person> getAll() {
        return map.values();
    }
}
