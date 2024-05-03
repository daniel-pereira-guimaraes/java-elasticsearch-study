package elastic;

import elastic.app.PersonService;
import elastic.infra.InMemoryPersonRepository;
import elastic.model.Person;
import elastic.model.PersonRepository;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Launcher {

    private static final PersonRepository personRepository = new InMemoryPersonRepository();
    private static final PersonService personService = new PersonService(personRepository);

    public static void main(String[] args) {
        var person = new Person("Daniel", LocalDate.of(1980, 12, 20), BigDecimal.valueOf(1000));
        personService.insert(person);

        var result = personService.get(person.id());

        System.out.printf("Inserted: " + person);
    }
}
