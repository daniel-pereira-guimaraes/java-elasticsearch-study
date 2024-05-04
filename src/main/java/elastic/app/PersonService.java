package elastic.app;

import elastic.model.Person;
import elastic.model.PersonNotFoundException;
import elastic.model.PersonRepository;

import java.util.Collection;

public class PersonService {
    private final PersonRepository repository;

    public PersonService(PersonRepository repository) {
        this.repository = repository;
    }

    public void insert(Person person) {
        repository.save(person);
    }

    public void update(Person person) {
        if (person.id() == null) {
            throw new IllegalArgumentException("Person ID is required for update!");
        }
        repository.save(person);
    }

    public Person get(String id) {
        var person = repository.get(id);
        return person.orElseThrow(() -> new PersonNotFoundException(id));
    }

    public Collection<Person> getAll() {
        return repository.getAll();
    }
}
