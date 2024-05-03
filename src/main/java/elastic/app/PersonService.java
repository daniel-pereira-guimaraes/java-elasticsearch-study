package elastic.app;

import elastic.model.Person;
import elastic.model.PersonNotFoundException;
import elastic.model.PersonRepository;

import java.util.Collection;
import java.util.List;

public class PersonService {
    private final PersonRepository repository;

    public PersonService(PersonRepository repository) {
        this.repository = repository;
    }

    public String insert(Person person) {
        repository.save(person);
        return person.id();
    }

    public Person get(String id) {
        var person = repository.get(id);
        return person.orElseThrow(() -> new PersonNotFoundException(id));
    }

    public Collection<Person> getAll() {
        return repository.getAll();
    }
}
