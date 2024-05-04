package elastic;

import com.google.gson.Gson;
import elastic.infra.JsonRepository;
import elastic.model.Person;
import elastic.model.PersonNotFoundException;
import elastic.model.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;

public class App {

    private static final Gson GSON = new Gson();
    private static final Repository<String> REPOSITORY = new JsonRepository("persons");

    public static void main(String[] args) {
        var person = new Person("Daniel", dateOf(1980, 12, 20), BigDecimal.valueOf(0));

        final var id = REPOSITORY.save(null, GSON.toJson(person));
        person.initialize(id);
        showPerson(person, "INSERTED");

        person.updateCreditLimit(BigDecimal.valueOf(1000));
        REPOSITORY.save(person.id(), GSON.toJson(person));

        var json = REPOSITORY.get(person.id().toString());
        if (json.isEmpty()) {
            throw new PersonNotFoundException(id);
        }
        person = GSON.fromJson(json.get(), Person.class);
        showPerson(person, "UPDATED");

        REPOSITORY.deleteIndex();
    }

    private static Date dateOf(int year, int month, int day) {
        return Date.from(LocalDate.of(year, month, day)
                .atStartOfDay()
                .toInstant(ZoneOffset.UTC));
    }

    private static void showPerson(Person person, String caption) {
        System.out.println("--- " + caption + " ---");
        System.out.println("id.........: " + person.id());
        System.out.println("name.......: " + person.name());
        System.out.println("birthDate..: " + person.birthDate());
        System.out.println("limitCredit: " + person.creditLimit());
        System.out.println();
    }
}
