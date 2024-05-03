package elastic;

import elastic.app.PersonService;
import elastic.infra.ElasticPersonRepository;
import elastic.model.Person;
import elastic.model.PersonRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;

public class Launcher {

    private static final PersonRepository personRepository = new ElasticPersonRepository();
    private static final PersonService personService = new PersonService(personRepository);

    public static void main(String[] args) {
        var person = new Person("Daniel", dateOf(1980, 12, 20), BigDecimal.valueOf(1000));
        personService.insert(person);
        System.out.printf("Inserted: " + person);
    }

    private static Date dateOf(int year, int month, int day) {
        return Date.from(LocalDate.of(year, month, day)
                .atStartOfDay()
                .toInstant(ZoneOffset.UTC));
    }
}
