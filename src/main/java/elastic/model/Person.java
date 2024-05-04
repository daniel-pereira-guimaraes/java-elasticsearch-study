package elastic.model;

import java.math.BigDecimal;
import java.util.Date;

public class Person {
    private String id;
    private final String name;
    private final Date birthDate;
    private BigDecimal creditLimit;

    public Person(String name, Date birthDate, BigDecimal creditLimit) {
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

    public Date birthDate() {
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
    public String toString() {
        return "Person {" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", birthDate=" + birthDate +
                ", creditLimit=" + creditLimit +
                '}';
    }
}
