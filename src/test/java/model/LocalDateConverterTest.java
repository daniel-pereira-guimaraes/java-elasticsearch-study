package model;

import elastic.model.LocalDateConverter;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class LocalDateConverterTest {

    @Test
    void convertLocalDateToInteger() {
        assertThat(LocalDateConverter.toInt(LocalDate.of(1969, 12, 31)), is(-1));
        assertThat(LocalDateConverter.toInt(LocalDate.of(1970, 1, 1)), is(0));
        assertThat(LocalDateConverter.toInt(LocalDate.of(1970, 1, 2)), is(1));
    }

    @Test
    void convertNullLocalDateToInteger() {
        assertThat(LocalDateConverter.toInt(null), nullValue());
    }

    @Test
    void convertIntegerToLocalDate() {
        assertThat(LocalDateConverter.fromInt(-1), is(LocalDate.of(1969, 12, 31)));
        assertThat(LocalDateConverter.fromInt(0), is(LocalDate.of(1970, 1, 1)));
        assertThat(LocalDateConverter.fromInt(1), is(LocalDate.of(1970, 1, 2)));
    }

    @Test
    void convertNullIntegerToLocalDate() {
        assertThat(LocalDateConverter.fromInt(null), nullValue());
    }

}
