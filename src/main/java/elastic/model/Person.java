package elastic.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Person {
    private String id;
    private final String name;
    private final LocalDate birthDate;
    private final BigDecimal creditLimit;

    public Person(String name, LocalDate birthDate, BigDecimal creditLimit) {
        this.name = name;
        this.birthDate = birthDate;
        this.creditLimit = creditLimit;
    }

    public void initialize(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public LocalDate birthDate() {
        return birthDate;
    }

    public BigDecimal creditLimit() {
        return creditLimit;
    }

    @Override
    public String toString() {
        return "Person {" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", birthDate=" + birthDate +
                ", creditLimit=" + creditLimit +
                '}';
    }
}
