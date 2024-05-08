package elastic.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class LocalDateConverter {

    private static final LocalDate REFERENCE = LocalDate.of(1970, 1, 1);

    private LocalDateConverter() {
    }

    public static Integer toInt(LocalDate date) {
        if (date == null) {
            return null;
        }
        long days = ChronoUnit.DAYS.between(REFERENCE, date);
        if (days < Integer.MIN_VALUE || days > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Cannot convert the date '" + date + "' to int.");
        }
        return (int) days;
    }

    public static LocalDate fromInt(Integer date) {
        return date == null ? null : REFERENCE.plusDays(date);
    }

}
