package elastic;

import elastic.infra.HttpClientJsonPersonRepository;
import elastic.infra.ElasticClientJsonPersonRepository;
import elastic.model.Person;
import elastic.model.PersonNotFoundException;
import elastic.model.PersonRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;

public class App {

    private static final String INDEX_NAME = "persons";
    private static final PersonRepository PERSON_REPOSITORY = choosePersonRepository();

    public static void main(String[] args) {
        try {
            PERSON_REPOSITORY.createIndex();
            try {
                insertPersons();
                insertUpdateGetPerson();
                waitForIndexing();
                getAllPersons();
                queryPersonByName();
                queryPersonByCreditLimit();
            } finally {
                PERSON_REPOSITORY.deleteIndex();
            }
        } finally {
            System.exit(0);
        }
    }

    private static void insertPersons() {
        insertPerson(new Person("John", dateOf(1980, 12, 20), BigDecimal.valueOf(1000)));
        insertPerson(new Person("Hilary", dateOf(1985, 8, 5), BigDecimal.valueOf(1500)));
        insertPerson(new Person("Anna Johnson", dateOf(1980, 11, 21), BigDecimal.valueOf(3000)));
        insertPerson(new Person("Joseph Johnson", dateOf(1980, 10, 22), BigDecimal.valueOf(2000)));
    }

    private static PersonRepository choosePersonRepository() {
        System.out.println("--- CHOOSE PERSON REPOSITORY ---");
        System.out.println();
        System.out.println("1 - " + ElasticClientJsonPersonRepository.class.getSimpleName());
        System.out.println("2 - " + HttpClientJsonPersonRepository.class.getSimpleName());
        var scanner = new Scanner(System.in);
        while (true) {
            System.out.print("\nWhat is your choice? ");
            var choice = scanner.nextByte();
            switch (choice) {
                case 1: return new ElasticClientJsonPersonRepository(INDEX_NAME);
                case 2: return new HttpClientJsonPersonRepository(INDEX_NAME);
                default: System.out.println("Invalid choice!");
            }
        }
    }

    private static void insertUpdateGetPerson() {
        var person = new Person("Emma", dateOf(1980, 12, 20), BigDecimal.valueOf(0));
        PERSON_REPOSITORY.save(person);
        showPerson(person, "INSERTED");
        person.updateCreditLimit(BigDecimal.valueOf(1500));
        PERSON_REPOSITORY.save(person);
        showPerson(person, "UPDATED");
        person = getById(person.id());
        showPerson(person, "GET");
    }

    private static Person getById(String id) {
        return PERSON_REPOSITORY.get(id).orElseThrow(
                () -> new PersonNotFoundException(id));
    }

    private static void getAllPersons() {
        showPersons(PERSON_REPOSITORY.getAll(), "ALL PERSONS");
    }

    private static void queryPersonByName() {
        var name = "johnson";
        var persons = PERSON_REPOSITORY.queryByName(name);
        showPersons(persons, "QUERY BY NAME: " + name);
    }

    private static void queryPersonByCreditLimit() {
        var min = BigDecimal.valueOf(1500);
        var max = BigDecimal.valueOf(2000);
        var persons = PERSON_REPOSITORY.queryByCreditLimit(min, max);
        showPersons(persons, "QUERY BY CREDIT LIMIT: " + min + ".." + max);
    }

    private static void insertPerson(Person person) {
        PERSON_REPOSITORY.save(person);
        showPerson(person, "INSERTED");
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
