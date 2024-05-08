package elastic.infra;

import elastic.model.Person;

import java.math.BigDecimal;
import java.util.Date;

public class PersonDocument {
    private String name;
    private Date birthDate;
    private BigDecimal creditLimit;

    public PersonDocument() {
    }

    private PersonDocument(Person person) {
        this.name = person.name();
        this.birthDate = person.birthDate();
        this.creditLimit = person.creditLimit();
    }

    public Person toPerson(String id) {
        return new Person(id, name, birthDate, creditLimit);
    }

    public static PersonDocument of(Person person) {
        return new PersonDocument(person);
    }

    public String getName() {
        return name;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }
}
