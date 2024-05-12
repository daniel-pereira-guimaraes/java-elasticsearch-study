package model;

import elastic.model.Person;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PersonTest {
    private static final LocalDate ANY_BIRTH_DATE = LocalDate.of(2000, 12, 15);
    private static final BigDecimal ANY_CREDIT_LIMIT = BigDecimal.TEN;
    public static final String ANY_NAME = "name1";
    public static final String ANY_ID = "id1";


    @Test
    void createCustomerWithId() {
        var person = new Person(ANY_ID, ANY_NAME, ANY_BIRTH_DATE, ANY_CREDIT_LIMIT, true);

        assertThat(person.id(), is(ANY_ID));
        assertThat(person.name(), is(ANY_NAME));
        assertThat(person.birthDate(), is(ANY_BIRTH_DATE));
        assertThat(person.creditLimit(), is(ANY_CREDIT_LIMIT));
        assertThat(person.isCustomer(), is(true));
    }

    @ParameterizedTest
    @CsvSource(value = {"null", "false"}, nullValues = {"null"})
    void createNonCustomerWithId(Boolean customer) {
        var person = new Person(ANY_ID, ANY_NAME, ANY_BIRTH_DATE, ANY_CREDIT_LIMIT, customer);

        assertThat(person.id(), is(ANY_ID));
        assertThat(person.name(), is(ANY_NAME));
        assertThat(person.birthDate(), is(ANY_BIRTH_DATE));
        assertThat(person.creditLimit(), is(ANY_CREDIT_LIMIT));
        assertThat(person.isCustomer(), is(false));
    }

    @Test
    void createPersonAndInitializeWithId() {
        var person = new Person(ANY_NAME, ANY_BIRTH_DATE, ANY_CREDIT_LIMIT, false);
        assertThat(person.id(), nullValue());

        person.initialize(ANY_ID);

        assertThat(person.id(), is(ANY_ID));
    }

    @Test
    void throwsExceptionWhenInitializingWithNullId() {
        var person = new Person(ANY_NAME, ANY_BIRTH_DATE, ANY_CREDIT_LIMIT, false);

        var exception = assertThrows(RuntimeException.class, () -> {
            person.initialize(null);
        });

        assertThat(exception.getMessage(), containsString("Cannot initialize with null id!"));
    }

    @Test
    void createPersonAndUpdateCreditLimitSuccessfully() {
        var person = new Person(ANY_ID, ANY_NAME, ANY_BIRTH_DATE, ANY_CREDIT_LIMIT, false);
        var newCreditLimit = ANY_CREDIT_LIMIT.add(BigDecimal.ONE);

        person.updateCreditLimit(newCreditLimit);

        assertThat(person.creditLimit(), is(newCreditLimit));
    }

    @ParameterizedTest
    @MethodSource("nullOrEmptyNameProvider")
    void throwsExceptionWhenCreatingPersonWithEmptyName(String nullOrEmptyName) {
        var exception = assertThrows(RuntimeException.class, () -> {
            new Person(ANY_ID, nullOrEmptyName, ANY_BIRTH_DATE, ANY_CREDIT_LIMIT, false);
        });

        assertThat(exception.getMessage(), containsString("Person name is required!"));
    }

    @Test()
    void throwsExceptionWhenUpdateCreditLimitWithNegativeValue() {
        var person = new Person(ANY_ID, ANY_NAME, ANY_BIRTH_DATE, ANY_CREDIT_LIMIT, false);
        var negativeValue = BigDecimal.ZERO.subtract(BigDecimal.ONE);

        var exception = assertThrows(IllegalArgumentException.class, () -> {
            person.updateCreditLimit(negativeValue);
        });

        assertThat(exception.getMessage(), containsString("Credit limit cannot be negative!"));
    }

    private static Stream<String> nullOrEmptyNameProvider() {
        return Stream.of(null, "", " ");
    }
}
