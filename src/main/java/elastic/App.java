package elastic;

import elastic.infra.ElasticClientPersonRepository;
import elastic.infra.HttpClientJsonPersonRepository;
import elastic.infra.ElasticClientJsonPersonRepository;
import elastic.model.Person;
import elastic.model.PersonNotFoundException;
import elastic.model.PersonRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Supplier;

public class App {

    private static final String INDEX_NAME = "persons";
    private static final PersonRepository PERSON_REPOSITORY = choosePersonRepository();

    public static void main(String[] args) {
        PERSON_REPOSITORY.createIndex();
        try {
            insertPersons();
            insertUpdateGetPerson();
            waitForIndexing();
            getAllPersons();
            getAllCustomers();
            queryPersonByName();
            queryPersonByCreditLimit();
        } finally {
            PERSON_REPOSITORY.deleteIndex();
        }
    }

    private static void insertPersons() {
        insertPerson(new Person("John", dateOf(1980, 12, 20), BigDecimal.valueOf(1000), false));
        insertPerson(new Person("Hilary", dateOf(1985, 8, 5), BigDecimal.valueOf(1500), true));
        insertPerson(new Person("Anna Johnson", dateOf(1980, 11, 21), BigDecimal.valueOf(3000), true));
        insertPerson(new Person("Joseph Johnson", dateOf(1980, 10, 22), BigDecimal.valueOf(2000), false));
    }

    private static PersonRepository choosePersonRepository() {
        var menuItems = getMenuItems();
        var userChoice = getUserChoice(menuItems);
        return menuItems.get(userChoice - 1).supplier.get();
    }

    private static int getUserChoice(List<MenuItem> menuItems) {
        var scanner = new Scanner(System.in);
        while (true) {
            System.out.println("--- CHOOSE PERSON REPOSITORY ---");
            System.out.println();
            printClassMenu(menuItems);
            System.out.print("\nWhat is your choice? ");
            try {
                var userChoice = scanner.nextByte();
                if (userChoice >= 1 && userChoice <= menuItems.size()) {
                    System.out.println();
                    return userChoice;
                }
            } catch (Exception e) {
                // Ignore!
            }
            System.out.println("Invalid option!");
            sleep(1000);
            System.out.println();
        }
    }

    private static void printClassMenu(List<MenuItem> menuItems) {
        var option = 0;
        for (var menuItem : menuItems) {
            System.out.println(++option + " - " + menuItem.clazz.getSimpleName());
        }
    }

    private static List<MenuItem> getMenuItems() {
        return List.of(
                new MenuItem(HttpClientJsonPersonRepository.class,
                        () -> new HttpClientJsonPersonRepository(INDEX_NAME)),
                new MenuItem(ElasticClientJsonPersonRepository.class,
                        () -> new ElasticClientJsonPersonRepository(INDEX_NAME)),
                new MenuItem(ElasticClientPersonRepository.class,
                        () -> new ElasticClientPersonRepository(INDEX_NAME))
        );
    }

    private static void insertUpdateGetPerson() {
        var person = new Person("Emma", dateOf(1980, 12, 20), BigDecimal.valueOf(0), false);
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
        showPersons(PERSON_REPOSITORY.getAll(false), "ALL PERSONS");
    }

    private static void getAllCustomers() {
        showPersons(PERSON_REPOSITORY.getAll(true), "ALL CUSTOMERS");
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

    private static LocalDate dateOf(int year, int month, int day) {
        return LocalDate.of(year, month, day);
    }

    private static void showPerson(Person person, String caption) {
        System.out.println("--- " + caption + " ---");
        System.out.println("id.........: " + person.id());
        System.out.println("name.......: " + person.name());
        System.out.println("birthDate..: " + person.birthDate());
        System.out.println("limitCredit: " + person.creditLimit());
        System.out.println("isCustomer: " + person.isCustomer());
        System.out.println();
    }

    private static void showPersons(Collection<Person> persons, String caption) {
        System.out.println("--- " + caption + " ---");
        persons.forEach(person ->
            System.out.println(person.id()
                    + " | " + padRight(person.name(), 15)
                    + " | " + person.birthDate()
                    + " | " + person.creditLimit()
                    + " | " + person.isCustomer())
        );
        System.out.println();
    }

    private static String padRight(Object object, int length) {
        var str = Objects.toString(object);
        return switch (Integer.compare(str.length(), length)) {
            case 0 -> str;
            case 1 -> str.substring(0, length);
            default -> str + " ".repeat(length - str.length());
        };
    }

    private static void waitForIndexing() {
        System.out.println("Waiting for indexing...");
        sleep(2000);
        System.out.println();
    }

    private static void sleep(int delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private record MenuItem(
            Class<? extends PersonRepository> clazz,
            Supplier<? extends PersonRepository> supplier) {
    }

}
