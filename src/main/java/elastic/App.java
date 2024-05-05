package elastic;

import com.google.gson.Gson;
import elastic.infra.HttpClientRepository;
import elastic.infra.JsonRepository;
import elastic.model.Person;
import elastic.model.PersonNotFoundException;
import elastic.model.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;

public class App {

    private static final String INDEX_NAME = "persons";
    private static final Gson GSON = new Gson();
    private static final Repository<String> REPOSITORY = chooseRepository();

    public static void main(String[] args) {
        REPOSITORY.createIndex();
        try {
            insertPerson(new Person("John", dateOf(1980, 12, 20), BigDecimal.valueOf(1000)));
            insertPerson(new Person("Hilary", dateOf(1985, 8, 5), BigDecimal.valueOf(1500)));
            insertPerson(new Person("Anna Johnson", dateOf(1980, 11, 21), BigDecimal.valueOf(3000)));
            insertPerson(new Person("Joseph Johnson", dateOf(1980, 10, 22), BigDecimal.valueOf(2000)));

            var id = insertAndUpdate();
            getById(id);

            waitForIndexing();

            getAll();
            queryByName();
            queryByCreditLimit();

        } finally {
            REPOSITORY.deleteIndex();
        }
        System.exit(0);
    }

    private static Repository<String> chooseRepository() {
        System.out.println("--- CHOOSE REPOSITORY ---");
        System.out.println();
        System.out.println("1 - JsonRepository");
        System.out.println("2 - HttpClientRepository");
        var scanner = new Scanner(System.in);
        while (true) {
            System.out.print("\nWhat is your choice? ");
            var choice = scanner.nextByte();
            switch (choice) {
                case 1: return new JsonRepository(INDEX_NAME);
                case 2: return new HttpClientRepository(INDEX_NAME);
                default: System.out.println("Invalid choice!");
            }
        }
    }

    private static String insertAndUpdate() {
        var person = new Person("Emma", dateOf(1980, 12, 20), BigDecimal.valueOf(0));
        var id = REPOSITORY.save(null, GSON.toJson(person));
        person.initialize(id);
        showPerson(person, "INSERTED");
        person.updateCreditLimit(BigDecimal.valueOf(1500));
        REPOSITORY.save(person.id(), GSON.toJson(person));
        showPerson(person, "UPDATED");
        return id;
    }

    private static void getById(String id) {
        var json = REPOSITORY.get(id);
        if (json.isEmpty()) {
            throw new PersonNotFoundException(id);
        }
        var person = GSON.fromJson(json.get(), Person.class);
        showPerson(person, "GET: " + id);
    }

    private static void getAll() {
        var persons = personsFromJsonMap(REPOSITORY.getAll());
        showPersons(persons, "ALL PERSONS");
    }

    private static void queryByName() {
        var name = "johnson";
        var persons = personsFromJsonMap(REPOSITORY.queryByName(name));
        showPersons(persons, "QUERY BY NAME: " + name);
    }

    private static void queryByCreditLimit() {
        var min = BigDecimal.valueOf(1500);
        var max = BigDecimal.valueOf(2000);
        var persons = personsFromJsonMap(REPOSITORY.queryByCreditLimit(min, max));
        showPersons(persons, "QUERY BY CREDIT LIMIT: " + min + ".." + max);
    }

    private static ArrayList<Person> personsFromJsonMap(Map<String, String> jsonMap) {
        var persons = new ArrayList<Person>();
        jsonMap.forEach((k, v) -> {
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
        persons.forEach(person ->
            System.out.println(person.id() + " | " + person.name() + " | " +
                    person.birthDate() + " | " + person.creditLimit())
        );
        System.out.println();
    }

    private static void waitForIndexing() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
