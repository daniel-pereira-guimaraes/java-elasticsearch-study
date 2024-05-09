package elastic.infra;

import elastic.model.LocalDateConverter;
import elastic.model.Person;

import java.math.BigDecimal;

public class PersonDocument {
    private String name;
    private Integer birthDate;
    private BigDecimal creditLimit;
    private Boolean customer;

    public PersonDocument() {
    }

    private PersonDocument(Person person) {
        this.name = person.name();
        this.birthDate = LocalDateConverter.toInt(person.birthDate());
        this.creditLimit = person.creditLimit();
        this.customer = person.isCustomer();
    }

    public Person toPerson(String id) {
        return new Person(id, name, LocalDateConverter.fromInt(birthDate), creditLimit, customer);
    }

    public static PersonDocument of(Person person) {
        return new PersonDocument(person);
    }

    public String getName() {
        return name;
    }

    public Integer getBirthDate() {
        return birthDate;
    }

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public Boolean getCustomer() {
        return customer;
    }
}
