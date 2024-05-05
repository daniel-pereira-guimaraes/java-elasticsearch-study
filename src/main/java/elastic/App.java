package elastic;

import com.google.gson.Gson;
import elastic.infra.JsonRepository;
import elastic.model.Person;
import elastic.model.PersonNotFoundException;
import elastic.model.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

public class App {

    private static final Gson GSON = new Gson();
    private static final Repository<String> REPOSITORY = new JsonRepository("persons");

    public static void main(String[] args) {
        insertPerson(new Person("John", dateOf(1980, 12, 20), BigDecimal.valueOf(1000)));
        insertPerson(new Person("Anna", dateOf(1980, 11, 21), BigDecimal.valueOf(3000)));
        insertPerson(new Person("Joseph", dateOf(1980, 10, 22), BigDecimal.valueOf(2000)));

        var id = insertAndUpdate();
        getById(id);
        getAll();
        queryByName();

        REPOSITORY.deleteIndex();
    }

    private static String insertAndUpdate() {
        var person = new Person("Emma", dateOf(1980, 12, 20), BigDecimal.valueOf(0));
        var id = REPOSITORY.save(null, GSON.toJson(person));
        person.initialize(id);
        person.updateCreditLimit(BigDecimal.valueOf(1500));
        REPOSITORY.save(person.id(), GSON.toJson(person));
        showPerson(person, "INSERTED");
        return id;
    }

    private static void getById(String id) {
        var json = REPOSITORY.get(id);
        if (json.isEmpty()) {
            throw new PersonNotFoundException(id);
        }
        var person = GSON.fromJson(json.get(), Person.class);
        showPerson(person, "UPDATED");
    }

    private static void getAll() {
        var persons = personsFromJsons(REPOSITORY.getAll());
        showPersons(persons, "ALL PERSONS");
    }

    private static void queryByName() {
        var persons = personsFromJsons(REPOSITORY.queryByName("Anna"));
        showPersons(persons, "QUERY BY NAME: Anna");
    }

    private static ArrayList<Person> personsFromJsons(Map<String, String> jsons) {
        var persons = new ArrayList<Person>();
        jsons.forEach((k, v) -> {
            var p = GSON.fromJson(v, Person.class);
            p.initialize(k);
            persons.add(p);
        });
        return persons;
    }

    private static void insertPerson(Person person) {
        var id = REPOSITORY.save(null, GSON.toJson(person));
        person.initialize(id);
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

    private static void showPersons(Collection<Person> persons, String caption) {
        System.out.println("--- " + caption + " ---");
        persons.forEach(person -> {
            System.out.println(person.id() + " | " + person.name() + " | " +
                    person.birthDate() + " | " + person.creditLimit());
        });
        System.out.println();
    }
}
