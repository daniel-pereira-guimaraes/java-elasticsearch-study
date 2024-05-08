package elastic.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class Person {
    private String id;
    private final String name;
    private final LocalDate birthDate;
    private BigDecimal creditLimit;

    public Person(String name, LocalDate birthDate, BigDecimal creditLimit) {
        this.name = name;
        this.birthDate = birthDate;
        this.creditLimit = validateCreditLimit(creditLimit);
    }

    public Person(String id, String name, LocalDate birthDate, BigDecimal creditLimit) {
        this.id = id;
        this.name = name;
        this.birthDate = birthDate;
        this.creditLimit = validateCreditLimit(creditLimit);
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

    public void updateCreditLimit(BigDecimal creditLimit) {
        this.creditLimit = validateCreditLimit(creditLimit);
    }

    private BigDecimal validateCreditLimit(BigDecimal creditLimit) {
        if (creditLimit != null && creditLimit.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Credit limit cannot be negative!");
        }
        return creditLimit;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        return equalsProperties((Person) other);
    }

    private boolean equalsProperties(Person person) {
        return Objects.equals(id, person.id)
                && Objects.equals(name, person.name)
                && Objects.equals(birthDate, person.birthDate)
                && Objects.equals(creditLimit, person.creditLimit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, birthDate);
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
