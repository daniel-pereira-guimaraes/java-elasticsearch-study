package elastic.model;

public class PersonNotFoundException extends RuntimeException {
    public PersonNotFoundException(String id) {
        super("Person not found: " + id);
    }
}
