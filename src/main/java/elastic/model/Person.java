package elastic.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class Person {
    private String id;
    private final String name;
    private final LocalDate birthDate;
    private BigDecimal creditLimit;
    private final Boolean customer;

    public Person(String name, LocalDate birthDate, BigDecimal creditLimit, Boolean customer) {
        this(null, name, birthDate, creditLimit, customer);
    }

    public Person(String id, String name, LocalDate birthDate, BigDecimal creditLimit, Boolean customer) {
        this.id = id;
        this.name = validateName(name);
        this.birthDate = birthDate;
        this.creditLimit = validateCreditLimit(creditLimit);
        this.customer = customer;
    }

    public void initialize(String id) {
        this.id = Objects.requireNonNull(id, "Cannot initialize with null id!");
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

    public boolean isCustomer() {
        return customer != null && customer;
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
                && Objects.equals(creditLimit, person.creditLimit)
                && Objects.equals(customer, person.customer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, birthDate, customer);
    }

    @Override
    public String toString() {
        return "Person {" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", birthDate=" + birthDate +
                ", creditLimit=" + creditLimit +
                ", customer=" + customer +
                '}';
    }

    private String validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Person name is required!");
        }
        return name;
    }
}
